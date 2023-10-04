package com.kuiz.demo.service;

import com.kuiz.demo.Dto.*;
import com.kuiz.demo.exception.SomethingException;
import com.kuiz.demo.model.Folder;
import com.kuiz.demo.model.PDF;
import com.kuiz.demo.model.User;
import com.kuiz.demo.repository.FolderRepository;
import com.kuiz.demo.repository.PDFRepository;
import com.kuiz.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FolderService {
    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PDFRepository pdfRepository;

    @Transactional
    public ResponseEntity<?> createFolder(Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        Map<String, Object> response = new HashMap<>();

        User currentUser = tempUser.get();

        Folder folder = Folder.builder()
                .folder_name("새폴더")
                .user(currentUser)
                .build();

        folderRepository.save(folder);
        response.put("message", "Folder가 성공적으로 만들어졌습니다.");
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<?> ChangePdfFolder(ChangePdfFolderDto changeFolderDto, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();
        Map<String, Object> response = new HashMap<>();

        PDF targetpdf = pdfRepository.findById(changeFolderDto.getPdf_id())
                .orElseThrow(() -> new SomethingException("관리자에게 문의해주세요."));
        Folder targetfolder = folderRepository.findById(changeFolderDto.getFolder_id())
                .orElseThrow(() -> new SomethingException("관리자에게 문의해주세요."));
        if(!targetpdf.getFolder().getUser().getUser_code().equals(currentUser.getUser_code()) ||
        !targetfolder.getUser().getUser_code().equals(currentUser.getUser_code())){
            throw new SomethingException("관리자에게 문의해주세요.");
        }

        targetpdf.setFolder(targetfolder);
        pdfRepository.save(targetpdf);

        response.put("message", "PDF의 폴더가 성공적으로 변경되었습니다.");
        return ResponseEntity.ok(response);

    }

    public ResponseEntity<?> GetUserFolder (Integer user_code){
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();

        UserFoldersDto userFoldersDto = new UserFoldersDto(currentUser);

        return ResponseEntity.ok(userFoldersDto);

    }

    @Transactional
    public ResponseEntity<?> updateFolderName(UpdateFolderNameDto updateFolderNameDto, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();
        Map<String, String> response = new HashMap<>();

        UserFoldersDto userFoldersDto = new UserFoldersDto(currentUser);

        boolean folderNameExists = userFoldersDto.getFolderDtos().stream()
                .anyMatch(folderDto -> folderDto.getFolder_name().equals(updateFolderNameDto.getFolder_name()));

        if (folderNameExists) {
            throw new SomethingException("중복된 폴더가 있습니다.");
        }

        boolean FolderExists = userFoldersDto.getFolderDtos().stream()
                .anyMatch(folderDto -> folderDto.getFolder_id().equals(updateFolderNameDto.getFolder_id()));
        if (!FolderExists){
            throw new SomethingException("관리자에게 문의해주세요.");
        }
        Optional<Folder> folderOptional = folderRepository.findById(updateFolderNameDto.getFolder_id());

        Folder folder = folderOptional.get();
        if(folder.getFolder_name().equals("default")){
            throw new SomethingException("관리자에게 문의해주세요.");
        }
        folder.setFolder_name(updateFolderNameDto.getFolder_name());
        folderRepository.save(folder);
        response.put("message", "정상적으로 변경되었습니다.");
        return ResponseEntity.ok(response);
    }

    public Optional<User> findUser(Integer user_code) {
        return userRepository.findById(user_code);
    }

}