package com.kuiz.demo.Controller;

import com.kuiz.demo.Dto.*;
import com.kuiz.demo.service.TestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TestService testService;

    @PostMapping("/create")
    public ResponseEntity<?> createTest(@RequestBody CreateTestRequireDto createTestRequireDto, HttpSession httpSession) {
        Integer user_code = (Integer) httpSession.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
        return testService.createTest(createTestRequireDto,user_code);
    }

//    @GetMapping("/gettest/{testId}")
//    public ResponseEntity<?> getTest(@PathVariable Integer testId,HttpSession session) {
//        Integer user_code = (Integer) session.getAttribute("user");
//        if (user_code == null) {
//            Map<String, String> responseMessage = new HashMap<>();
//            responseMessage.put("error", "로그인하지 않았습니다.");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
//        }
//        return testService.getTest(testId, user_code);
//    }

//    @GetMapping("/getanswer/{testId}")
//    public ResponseEntity<?> getanswer(@PathVariable Integer testId,HttpSession session) {
//        Integer user_code = (Integer) session.getAttribute("user");
//        if (user_code == null) {
//            Map<String, String> responseMessage = new HashMap<>();
//            responseMessage.put("error", "로그인하지 않았습니다.");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
//        }
//        return testService.getTestWithAnser(testId, user_code);
//    }

//    @PostMapping("/create")
//    public ResponseEntity<?> createTest(@RequestBody CreateTestRequireDto createTestRequireDto) {
//        List<QuestionDto> questionDtos = new ArrayList<>();
//
//        if(createTestRequireDto.getMultiple_choices()!= null) {
//            for (int i = 0; i < createTestRequireDto.getMultiple_choices(); i++) {
//                QuestionDto questionDto = new QuestionDto();
//                questionDto.setType("multiple_choices");
//                questionDto.setQuestion("Question MP" + (i + 1) + "?");
//                questionDto.setChoices(Arrays.asList("Choice 1", "Choice 2", "Choice 3", "Choice 4"));
//                questionDto.setUser_answer(null);
//                questionDtos.add(questionDto);
//            }
//        }
//
//        if(createTestRequireDto.getN_multiple_choices()!= null){
//            for (int i = 0; i < createTestRequireDto.getN_multiple_choices(); i++) {
//                QuestionDto questionDto = new QuestionDto();
//                questionDto.setType("multiple_choices");
//                questionDto.setQuestion("Question MP" + (i + 1) + "?");
//                questionDto.setChoices(null);
//                questionDto.setChoices(null);
//                questionDtos.add(questionDto);
//            }
//        }
//
//
//        TestDto test = new TestDto(createTestRequireDto.getPdf_id());
//        test.setQuestions(questionDtos);
//
//        return ResponseEntity.ok(test); // OK (200) status is sent along with the test data.
//    }

    @GetMapping("/gettest/{testId}")
    public ResponseEntity<?> getTest(@PathVariable Integer testId,HttpSession session) {
        List<QuestionDto> questionDtos = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            QuestionDto questionDto = new QuestionDto();
            questionDto.setType("multiple_choices");
            questionDto.setQuestion("Question MP" + (i + 1) + "?");
            questionDto.setChoices(Arrays.asList("Choice 1", "Choice 2", "Choice 3", "Choice 4"));
            questionDto.setUser_answer(null);
            questionDtos.add(questionDto);

            QuestionDto questionDto1 = new QuestionDto();
            questionDto1.setType("N_multiple_choices");
            questionDto1.setQuestion("Question NMP" + (i + 1) + "?");
            questionDto1.setChoices(null);
            questionDto1.setUser_answer(null);
            questionDtos.add(questionDto1);
        }

        TestDto test = new TestDto(1);
        test.setQuestions(questionDtos);

        return ResponseEntity.ok(test); // OK (200) status is sent along with the test data.
    }


    @GetMapping("/getanswer/{testId}")
    public ResponseEntity<?> getanswer(@PathVariable Integer testId,HttpSession session) {
        List<QuestionWithAnswerDto> questionWithAnswerDtos = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            QuestionWithAnswerDto questionWithAnswerDto = new QuestionWithAnswerDto();
            questionWithAnswerDto.setType("multiple_choices");
            questionWithAnswerDto.setQuestion("Question MP" + (i + 1) + "?");
            questionWithAnswerDto.setChoices(Arrays.asList("Choice 1", "Choice 2", "Choice 3", "Choice 4"));
            questionWithAnswerDto.setAnswer("Answer");
            questionWithAnswerDto.setUser_answer("User Answer");
            questionWithAnswerDto.setExplanation("Explanation");
            if (i%2==0){
                questionWithAnswerDto.setCorrect(true);
            }
            else {
                questionWithAnswerDto.setCorrect(false);
            }
            questionWithAnswerDtos.add(questionWithAnswerDto);

            QuestionWithAnswerDto questionWithAnswerDto1 = new QuestionWithAnswerDto();
            questionWithAnswerDto1.setType("N_multiple_choices");
            questionWithAnswerDto1.setQuestion("Question MP" + (i + 1) + "?");
            questionWithAnswerDto1.setAnswer("Answer");
            questionWithAnswerDto1.setUser_answer("User Answer");
            questionWithAnswerDto1.setExplanation("Explanation");
            questionWithAnswerDto1.setChoices(null);
            if (i%2==0){
                questionWithAnswerDto1.setCorrect(true);
            }
            else {
                questionWithAnswerDto1.setCorrect(false);
            }
            questionWithAnswerDtos.add(questionWithAnswerDto1);
        }

        TestWithAnswerDto test = new TestWithAnswerDto(1);
        test.setQuestions(questionWithAnswerDtos);

        return ResponseEntity.ok(test); // OK (200) status is sent along with the test data.
    }

    @GetMapping("/my-tests")
    public ResponseEntity<?> myTests(HttpSession session) {
//        Integer user_code = (Integer) session.getAttribute("user");
//        if (user_code == null) {
//            Map<String, String> responseMessage = new HashMap<>();
//            responseMessage.put("error", "로그인하지 않았습니다.");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
//        }

        List<MytestDto> tests = new ArrayList<>();
        for(int i=0;i<5;i++){
            MytestDto mytestDto = new MytestDto();
            mytestDto.setTest_id(i);
            mytestDto.setTest_name("test"+(i+1));
            mytestDto.setDate("2023.10."+(i+10));
            mytestDto.setSubject("과목"+(i+1));
            mytestDto.setFolder_name("파일"+(i+1));
            mytestDto.setPage(i+15);
            tests.add(mytestDto);
        }

        Map<String, Object> response =new HashMap<>();
        response.put("tests",tests);
        return ResponseEntity.ok(response);
    }

}
