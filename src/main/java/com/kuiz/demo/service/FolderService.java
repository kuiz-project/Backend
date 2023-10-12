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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.*;

@Service
public class FolderService {
    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PDFRepository pdfRepository;

    @Autowired
    private S3Uploader s3Uploader;

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

        Optional<Folder> folderOptional = folderRepository.findById(updateFolderNameDto.getFolder_id());

        Folder folder = folderOptional.get();

        if(!folder.getUser().getUser_code().equals(user_code)){
            throw new SomethingException("관리자에게 문의해주세요.");
        }

        folder.setFolder_name(updateFolderNameDto.getFolder_name());
        folderRepository.save(folder);
        response.put("message", "정상적으로 변경되었습니다.");
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<?> deleteFolder(Integer folderId, Integer userCode) {
        Optional<User> tempUser = findUser(userCode);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();
        Map<String, Object> response = new HashMap<>();

        // 해당 폴더가 DB에 있는지 확인
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new SomethingException("삭제하려는 폴더를 찾을 수 없습니다."));

        // 폴더의 소유자가 현재 유저인지 확인
        if(!folder.getUser().getUser_code().equals(currentUser.getUser_code())){
            throw new SomethingException("관리자에게 문의해주세요");
        }

        // 폴더 내 모든 PDF 삭제
        List<PDF> pdfsInFolder = folder.getPdfs();
        List<Integer> pdfIdsInFolder = new ArrayList<>();
        for (PDF pdf : pdfsInFolder) {
            pdfIdsInFolder.add(pdf.getPdf_id());
        }

        // PDF 삭제
        DeletePDFsResult deletePDFsResult = deletePDFs(pdfIdsInFolder,userCode);
        if (!deletePDFsResult.isSuccess()) {
            response.put("message", deletePDFsResult.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // 폴더 삭제
        folderRepository.delete(folder);

        response.put("message", "폴더가 성공적으로 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }


    public Optional<User> findUser(Integer user_code) {
        return userRepository.findById(user_code);
    }

    @Transactional
    public DeletePDFsResult deletePDFs(List<Integer> pdfIds ,Integer user_code) {

        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()) {
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();

        UserPdfDto userPdfDto = new UserPdfDto(currentUser);

        // First loop: Validate the PDFs
        for (Integer pdfId : pdfIds) {
            boolean PDFExists = userPdfDto.getPdf().stream()
                    .anyMatch(folderDto -> folderDto.getPdf_id().equals(pdfId));
            if (!PDFExists) {
                return new DeletePDFsResult(false, "권한이 없습니다. 관리자에게 문의해주세요.");
            }
        }

        // Second loop: Delete the PDFs
        for (Integer pdfId : pdfIds) {
            Optional<PDF> pdfOptional = pdfRepository.findById(pdfId);
            if (pdfOptional.isPresent()) {
                PDF pdf = pdfOptional.get();
                s3Uploader.deleteS3(pdf.getFile_url());  // Consider moving this after repository deletion depending on your use case
                pdfRepository.delete(pdf);
            }
        }

        return new DeletePDFsResult(true, "folder 내 파일을 성공적으로 삭제했습니다");
    }

}