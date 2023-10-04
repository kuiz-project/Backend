package com.kuiz.demo.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuiz.demo.Dto.*;
import com.kuiz.demo.exception.SomethingException;
import com.kuiz.demo.model.PDF;
import com.kuiz.demo.model.Test;
import com.kuiz.demo.model.User;
import com.kuiz.demo.repository.*;
import jakarta.transaction.Transactional;
import org.json.JSONArray;
import org.json.JSONObject;
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
    public ResponseEntity<?> createTest(CreateTestDto createTestDto, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        Optional<PDF> tempPDF = pdfRepository.findById(createTestDto.getPdf_id());

        if (!tempUser.isPresent() || !tempPDF.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }

        User currentUser = tempUser.get();
        PDF currentPDF = tempPDF.get();
        Map<String, String> response = new HashMap<>();

        if (!currentUser.getUser_code().equals(currentPDF.getUser().getUser_code())){
            throw new SomethingException("잘못된 접근입니다.");
        }

        JSONObject json = new JSONObject(currentPDF.getKeywords());
        JSONArray keywordArray = json.getJSONArray(String.valueOf(createTestDto.getPage()));
        //파이썬 코드 실행 (인자는 keywordarray, 객관식, 주관식 갯수)



        return ResponseEntity.ok(response);
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

        Map<String, Object> response = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        List<QuestionWithAnswerDto> questionWithAnswerDtos = null;

        try {
            questionWithAnswerDtos = objectMapper.readValue(currentTest.getQuestions(), new TypeReference<List<QuestionWithAnswerDto>>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new SomethingException("QuestionDto JSON 파싱 중 오류가 발생했습니다.");
        }

        TestWithAnswerDto testWithAnswerDto = new TestWithAnswerDto(testId);
        testWithAnswerDto.setQuestions(questionWithAnswerDtos);
        response.put("message",testWithAnswerDto);

        return ResponseEntity.ok(response);
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

        Map<String, Object> response = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        List<QuestionDto> questionDto = null;

        try {
            questionDto = objectMapper.readValue(currentTest.getQuestions(), new TypeReference<List<QuestionDto>>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new SomethingException("QuestionDto JSON 파싱 중 오류가 발생했습니다.");
        }

        TestDto testDto = new TestDto(testId);
        testDto.setQuestions(questionDto);
        response.put("message",testDto);

        return ResponseEntity.ok(response);
    }

    public Optional<User> findUser(Integer user_code) {
        return userRepository.findById(user_code);
    }

    public String callPythonScript() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", "your_python_script.py");
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            return output.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
