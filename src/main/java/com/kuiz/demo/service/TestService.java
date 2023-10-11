package com.kuiz.demo.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuiz.demo.Dto.*;
import com.kuiz.demo.exception.SomethingException;
import com.kuiz.demo.model.PDF;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import com.kuiz.demo.model.QuestionData;
import com.kuiz.demo.model.Test;
import com.kuiz.demo.model.User;
import com.kuiz.demo.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        String pythonPath = "/path/to/your/script.py";

        String pythonOutput = executePythonScript(pythonPath, jsonString);
        QuestionData questionData;
        try {
            questionData = objectMapper.readValue(pythonOutput, QuestionData.class);
        } catch (Exception e) {
            throw new RuntimeException("파이썬코드 실행 이후 역직렬화 error",e);
        }



        Test test = Test.builder()
                .test_name(getCurrentLocalTimeAsString()+currentPDF.getFile_name())
                .multiple_choices(createTestRequireDto.getMultiple_choices())
                .N_multiple_choices(createTestRequireDto.getN_multiple_choices())
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

        TestDto testDto = new TestDto(testId);
        testDto.setQuestions(questionDto);

        return ResponseEntity.ok(testDto);
    }

    public Optional<User> findUser(Integer user_code) {
        return userRepository.findById(user_code);
    }

    public String executePythonScript(String pythonScriptPath, String jsonInput) {
        ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath);
        processBuilder.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();

        try {
            Process process = processBuilder.start();

            // 입력을 파이썬 스크립트에 제공
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(jsonInput);
            writer.flush();
            writer.close();

            // 파이썬 스크립트의 출력을 받기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
                output.append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python script exited with code: " + exitCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error executing python script", e);
        }

        return output.toString().trim();  // 파이썬 스크립트에서 반환된 결과
    }

    public String getCurrentLocalTimeAsString() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH:mm");
        return now.format(formatter);
    }
}
