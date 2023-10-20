package com.kuiz.demo.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuiz.demo.Dto.*;
import com.kuiz.demo.exception.SomethingException;
import com.kuiz.demo.model.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.kuiz.demo.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PDFRepository pdfRepository;

    @Autowired
    private TestRepository testRepository;

    @Transactional
    public ResponseEntity<?> createTest(CreateTestRequireDto createTestRequireDto, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        Optional<PDF> tempPDF = pdfRepository.findById(createTestRequireDto.getPdf_id());

        if (!tempUser.isPresent() || !tempPDF.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }

        User currentUser = tempUser.get();
        PDF currentPDF = tempPDF.get();

        if (!currentUser.getUser_code().equals(currentPDF.getUser().getUser_code())){
            throw new SomethingException("잘못된 접근입니다.");
        }

        CreateQuestionDto createQuestionDto = new CreateQuestionDto();
        createQuestionDto.setKeywords(currentPDF.getKeywords().getPageKeywords().get(createTestRequireDto.getPage().toString()));
        createQuestionDto.setMultiple_choices(createTestRequireDto.getMultiple_choices());
        createQuestionDto.setN_multiple_choices(createTestRequireDto.getN_multiple_choices());
        createQuestionDto.setSubject(currentPDF.getSubject());

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(createQuestionDto);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing createQuestionDto", e);
        }

        //파이썬 코드 실행 (인자는 keywordarray, 객관식, 주관식 갯수,subject)
        String pythonPath = "/home/master/python/get_test.py";

        String pythonOutput = executePythonScript(pythonPath, jsonString);
        QuestionData questionData;
        try {
            questionData = objectMapper.readValue(pythonOutput, QuestionData.class);
            questionData.getQuestions().stream().map(temp->{
                if (temp.getChoices()==null){
                    temp.setType("N_multiple_choices");
                }
                else temp.setType("multiple_choices");
                return temp;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("파이썬코드 실행 이후 역직렬화 error",e);
        }

        Test test = Test.builder()
                .page(createTestRequireDto.getPage())
                .multiple_choices(createTestRequireDto.getMultiple_choices())
                .N_multiple_choices(createTestRequireDto.getN_multiple_choices())
                .date(getCurrentLocalTimeAsString())
                .questionData(questionData)
                .user(currentUser)
                .pdf(currentPDF)
                .build();

         Test savedTest = testRepository.save(test);

        return getTest(savedTest.getTest_id(), user_code);
    }

    @Transactional
    public ResponseEntity<?> getTestWithAnser(Integer testId, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        Optional<Test> tempTest = testRepository.findById(testId);
        if (!tempUser.isPresent() || !tempTest.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();
        Test currentTest = tempTest.get();

        if (!currentTest.getUser().getUser_code().equals(currentUser.getUser_code())){
            throw new SomethingException("잘못된 접근입니다.");
        }

        ModelMapper modelMapper = new ModelMapper();

        List<QuestionWithAnswerDto> questionWithAnswerDtos = currentTest.getQuestionData().getQuestions().stream()
                .map(question -> modelMapper.map(question, QuestionWithAnswerDto.class))
                .collect(Collectors.toList());

        TestWithAnswerDto testWithAnswerDto = new TestWithAnswerDto(testId);
        testWithAnswerDto.setQuestions(questionWithAnswerDtos);

        return ResponseEntity.ok(testWithAnswerDto);

    }

    @Transactional
    public ResponseEntity<?> getTest(Integer testId, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        Optional<Test> tempTest = testRepository.findById(testId);
        if (!tempUser.isPresent() || !tempTest.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();
        Test currentTest = tempTest.get();

        if (!currentTest.getUser().getUser_code().equals(currentUser.getUser_code())){
            throw new SomethingException("잘못된 접근입니다.");
        }

        ModelMapper modelMapper = new ModelMapper();

        List<QuestionDto> questionDto = currentTest.getQuestionData().getQuestions().stream()
                .map(question -> modelMapper.map(question, QuestionDto.class))
                .collect(Collectors.toList());

        TestDto testDto = new TestDto();
        testDto.setTest_id(testId);
        testDto.setQuestions(questionDto);

        return ResponseEntity.ok(testDto);
    }

    @Transactional
    public ResponseEntity<?> getmytests(Integer user_code){
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();

        List<MytestDto> tests = currentUser.getTests().stream().map(temp -> {
            PDF pdf = temp.getPdf();
            Folder folder = pdf.getFolder();
            MytestDto mytestDto = new MytestDto();
            mytestDto.setTest_id(temp.getTest_id());
            mytestDto.setSubject(pdf.getSubject());
            mytestDto.setFile_name(pdf.getFile_name());
            mytestDto.setFolder_name(folder.getFolder_name());
            mytestDto.setPage(temp.getPage());
            mytestDto.setDate(temp.getDate());
            mytestDto.setScore(temp.getScore());
            return mytestDto;
        }).collect(Collectors.toList());

        Map<String, Object> response =new HashMap<>();
        response.put("tests",tests);
        return ResponseEntity.ok(tests);
    }
    @Transactional
    public ResponseEntity<?> scoreTest(TestDto testDto, Integer user_code){
        Optional<User> tempUser = findUser(user_code);
        Optional<Test> tempTest = testRepository.findById(testDto.getTest_id());
        if (!tempUser.isPresent() || !tempTest.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();
        Test currentTest = tempTest.get();
        if (!currentTest.getUser().getUser_code().equals(currentUser.getUser_code())){
            throw new SomethingException("잘못된 접근입니다.");
        }
        List<Question> questions = currentTest.getQuestionData().getQuestions();
        List<QuestionDto> questionDtos = testDto.getQuestions();
        List<ScoreQuestionDto> scoreQuestionDtoList = new ArrayList<>();
        Integer correct = 0;
        for(int i = 0; i < questions.size(); i++) {
            Question currentQuestion = questions.get(i);
            QuestionDto currentDto = questionDtos.get(i);

            currentQuestion.setUser_answer(currentDto.getUser_answer());
            if(currentQuestion.getType().equals("N_multiple_choices")&&currentQuestion.getAnswer().equals(currentDto.getUser_answer())){
                currentQuestion.setCorrect(true);
                correct=correct+1;
            }
            else {
                currentQuestion.setCorrect(false);
                ScoreQuestionDto tempScore = convertToScoreQuestionDto(currentQuestion);
                tempScore.setSequence(i);
                scoreQuestionDtoList.add(tempScore);
            }

        }
        if (!scoreQuestionDtoList.isEmpty()) {
            ScoreTestDto scoreTestDto = new ScoreTestDto();
            scoreTestDto.setQuestions(scoreQuestionDtoList);


            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString;
            try {
                jsonString = objectMapper.writeValueAsString(scoreTestDto);
            } catch (Exception e) {
                throw new RuntimeException("Error serializing createQuestionDto", e);
            }
            //파이썬 코드 실행
            String pythonPath = "/home/master/python/get_score.py";

            String pythonOutput = executePythonScript(pythonPath, jsonString);
            ScoreTestListResponse scoreTestListResponse;
            try {
                scoreTestListResponse = objectMapper.readValue(pythonOutput, ScoreTestListResponse.class);
            } catch (Exception e) {
                throw new RuntimeException("파이썬코드 실행 이후 역직렬화 error", e);
            }

            //주관식 정답 수정
            for (int i=0;i<scoreTestListResponse.getQuestions().size();i++ ){
                ScoreTestRequestDto testRequestDto = scoreTestListResponse.getQuestions().get(i);
                Question currentQuestion = questions.get(testRequestDto.getSequence()); //원래 DB가져오기
                currentQuestion.setCorrect(testRequestDto.isCorrect()); //수정
                if (testRequestDto.isCorrect()) correct = correct+1;
            }

            currentTest.setScore(correct+"/"+(currentTest.getMultiple_choices()+currentTest.getN_multiple_choices()));
        }
        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", "채점 완료");
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    public ScoreQuestionDto convertToScoreQuestionDto(Question questionWithAnswerDto) {
        ScoreQuestionDto scoreQuestionDto = new ScoreQuestionDto();

        scoreQuestionDto.setQuestion(questionWithAnswerDto.getQuestion());
        scoreQuestionDto.setAnswer(questionWithAnswerDto.getAnswer());
        scoreQuestionDto.setUser_answer(questionWithAnswerDto.getUser_answer());
        // 다른 필드들 (answer, explanation, correct)는 초기화 되지 않았으므로 필요한 경우 적절한 값으로 설정하십시오.

        return scoreQuestionDto;
    }
    public Optional<User> findUser(Integer user_code) {
        return userRepository.findById(user_code);
    }

    private String executePythonScript(String scriptPath, String jsonString) {
        ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath);
        processBuilder.redirectErrorStream(true);
        StringBuilder output = new StringBuilder();

        try {
            Process process = processBuilder.start();
            try (
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
            ) {
                // jsonString을 파이프를 통해 전달
                writer.write(jsonString);
                writer.flush();
                writer.close();

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                    output.append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("파이썬 스크립트가 다음 코드로 종료되었습니다: " + exitCode + " 출력:\n" + output.toString());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("파이썬 스크립트 실행 중 오류 발생", e);
        }

        return output.toString().trim();

    }

    public String getCurrentLocalTimeAsString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return now.format(formatter);
    }
}
