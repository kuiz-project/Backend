package com.kuiz.demo.Controller;

import com.kuiz.demo.Dto.ChangePdfFolderDto;
import com.kuiz.demo.Dto.UpdateFolderNameDto;
import com.kuiz.demo.service.FolderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/folder")
public class FolderController {
    @Autowired
    FolderService folderService;

    @PostMapping("/create")
    public ResponseEntity<?> createFolder( HttpSession httpSession) {
        Integer user_code = (Integer) httpSession.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
        return folderService.createFolder(user_code);
    }

    @PostMapping("/change-folder")
    public ResponseEntity<?> ChangeFolder(@RequestBody ChangePdfFolderDto changeFolderDto, HttpSession httpSession) {
        Integer user_code = (Integer) httpSession.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
        return folderService.ChangePdfFolder(changeFolderDto,user_code);
    }

    @GetMapping("my-folders")
    public ResponseEntity<?> getMyFolders(HttpSession session) {
        Integer user_code = (Integer) session.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
        return folderService.GetUserFolder(user_code);
    }

    @PatchMapping("/update-name")
    public ResponseEntity<?> updateFolderName(@RequestBody UpdateFolderNameDto updateFolderNameDto,
                                           HttpSession session) {
        Integer user_code = (Integer) session.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }

        return folderService.updateFolderName(updateFolderNameDto, user_code);
    }

    @DeleteMapping("/delete/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable Integer folderId, HttpSession httpSession) {
        Integer user_code = (Integer) httpSession.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }

        return folderService.deleteFolder(folderId, user_code);

    }
}
