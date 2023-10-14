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
                .folder_name("default")
                .user(user)
                .build();
        folderRepository.save(folder);

        Map<String, String> responseMessage = new HashMap<>();
        responseMessage.put("message", "회원가입 완료");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMessage);
    }

    @Transactional
    public ResponseEntity<?> login(LoginRequestDto dto, HttpSession session, HttpServletResponse httpResponse) {
        Optional<User> optionalUser = userRepository.findByIdentifier(dto.getId());
        Map<String, Object> response = new HashMap<>();

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPassword().equals(dto.getPassword())) {
                session.setAttribute("user", user.getUser_code());
                response.put("name", user.getName());

                // 쿠키 설정 예제. 적절히 설정이 필요합니다.
                Cookie cookie = new Cookie("JSESSIONID", session.getId());
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                httpResponse.addCookie(cookie);

                // 기존의 "Set-Cookie" 헤더를 모두 가져와서 새로운 "Set-Cookie" 헤더로 설정합니다.
                Collection<String> cookiesHeaders = httpResponse.getHeaders("Set-Cookie");
                httpResponse.setHeader("Set-Cookie", null);
                for (String header : cookiesHeaders) {
                    // SameSite=None 설정 시 Secure flag 도 활성화 되어야 함
                    httpResponse.addHeader("Set-Cookie", header + "; SameSite=None; Secure");
                }
                return new ResponseEntity<>(response, HttpStatus.OK);
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
