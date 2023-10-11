package com.kuiz.demo.Controller;

import com.kuiz.demo.Dto.CreateTestRequireDto;
import com.kuiz.demo.service.TestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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

    @GetMapping("/gettest/{testId}")
    public ResponseEntity<?> getTest(@PathVariable Integer testId,HttpSession session) {
        Integer user_code = (Integer) session.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
        return testService.getTest(testId, user_code);
    }

    @GetMapping("/getanswer/{testId}")
    public ResponseEntity<?> getanswer(@PathVariable Integer testId,HttpSession session) {
        Integer user_code = (Integer) session.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
        return testService.getTestWithAnser(testId, user_code);
    }
}
