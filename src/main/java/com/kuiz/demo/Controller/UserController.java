package com.kuiz.demo.Controller;

import com.kuiz.demo.Dto.LoginRequestDto;
import com.kuiz.demo.Dto.PasswordChangeRequestDto;
import com.kuiz.demo.Dto.SignUpRequestDto;
import com.kuiz.demo.model.User;
import com.kuiz.demo.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        return userService.signUp(signUpRequestDto);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto, HttpSession session) {
        return userService.login(dto, session);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        return userService.logout(session, response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequestDto dto, HttpSession session) {
        Integer UserId = (Integer) session.getAttribute("user");

        if (UserId == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
        return userService.changePassword(dto, UserId);
    }

    @GetMapping("/findId/{userId}")
    public ResponseEntity<?> findUser(@PathVariable String userId) {
        return userService.checkId(userId);
    }

}