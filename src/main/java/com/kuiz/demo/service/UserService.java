package com.kuiz.demo.service;

import com.kuiz.demo.Dto.LoginRequestDto;
import com.kuiz.demo.Dto.PasswordChangeRequestDto;
import com.kuiz.demo.Dto.SignUpRequestDto;
import com.kuiz.demo.exception.SomethingException;
import com.kuiz.demo.model.Folder;
import com.kuiz.demo.model.User;
import com.kuiz.demo.repository.FolderRepository;
import com.kuiz.demo.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Transactional
    public ResponseEntity<?> signUp(SignUpRequestDto signUpRequestDto){
        User user = User.builder()
                .identifier(signUpRequestDto.getId())
                .password(signUpRequestDto.getPassword())
                .email(signUpRequestDto.getEmail())
                .name(signUpRequestDto.getName())
                .build();
        userRepository.save(user);
        Folder folder = Folder.builder()
                .folder_name("빈폴더")
                .user(user)
                .build();
        folderRepository.save(folder);

        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", "회원가입 완료");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMessage);
    }

    @Transactional
    public ResponseEntity<?> login(LoginRequestDto dto, HttpSession session, HttpServletResponse response) {
        Optional<User> optionalUser = userRepository.findByIdentifier(dto.getId());
        Map<String, Object> responseData = new HashMap<>();

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPassword().equals(dto.getPassword())) {
                session.setAttribute("user", user.getUser_code());
                responseData.put("name", user.getName());

                // 쿠키 설정
                Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
                sessionCookie.setPath("/");  // 쿠키의 유효 경로 설정
                sessionCookie.setHttpOnly(true);  // JavaScript를 통한 접근 방지
                sessionCookie.setSecure(true);  // HTTPS에서만 전송
                response.addCookie(sessionCookie);  // 응답에 쿠키 추가

                // SameSite 속성 추가
                String newHeader = String.format("%s; SameSite=None", response.getHeader("Set-Cookie"));
                response.setHeader("Set-Cookie", newHeader);

                return new ResponseEntity<>(responseData, HttpStatus.OK);
            }
        }
        throw new SomethingException("존재하지 않는 사용자이거나 비밀번호가 틀립니다.");
    }


    @Transactional
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        session.removeAttribute("user");

        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", "로그아웃 완료");
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> changePassword(PasswordChangeRequestDto dto,Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        else{
            User currentUser = tempUser.get();
            if (currentUser.getPassword().equals(dto.getOldPassword())) {
                currentUser.setPassword(dto.getNewPassword());
                userRepository.save(currentUser);

                Map<String, String> responseMessage = new HashMap<>();
                responseMessage.put("message", "비밀번호 변경 완료");
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            } else {
                throw new SomethingException("비밀번호가 틀립니다.");
            }
        }

    }

    public ResponseEntity<?> checkId(String Id) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> optionalUser = userRepository.findByIdentifier(Id);
        if (optionalUser.isPresent()){
            response.put("error", "사용중인 아이디가 있습니다.");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        else{
            response.put("message", "사용가능한 아이디입니다.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    public Optional<User> findUser(Integer UserId) {
        return userRepository.findById(UserId);
    }
}
