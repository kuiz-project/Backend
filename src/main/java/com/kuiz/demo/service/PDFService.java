package com.kuiz.demo.service;

import com.kuiz.demo.Dto.*;
import com.kuiz.demo.exception.SomethingException;
import com.kuiz.demo.model.Folder;
import com.kuiz.demo.model.PDF;
import com.kuiz.demo.model.User;
import com.kuiz.demo.repository.FolderRepository;
import com.kuiz.demo.repository.PDFRepository;
import com.kuiz.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PDFService {

    @Autowired
    private PDFRepository pdfRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3Uploader s3Uploader;

    @Transactional
    public ResponseEntity<?> uploadPDF(MultipartFile multipartFile, String subject, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();

        Optional<Folder> tempDefaultFolder = currentUser.getFolders().stream()
                .filter(folder -> folder.getFolder_name().equals("default"))
                .findFirst();

        Folder defaultFolder;

        if (!tempDefaultFolder.isPresent()){
            defaultFolder = Folder.builder()
                    .folder_name("default")
                    .user(currentUser)
                    .build();
            folderRepository.save(defaultFolder);
        }
        else{
            defaultFolder = tempDefaultFolder.get();
        }

        // 먼저 PDF 정보를 데이터베이스에 저장
        PDF pdf = PDF.builder()
                .file_name(multipartFile.getOriginalFilename())
                .subject(subject)
                .folder(defaultFolder)
                .user(currentUser)
                .build();
        PDF savedPdf = pdfRepository.save(pdf);

        // 데이터베이스 저장 후 S3에 파일 업로드
        String fileUrl = s3Uploader.uploadFileToS3(multipartFile, user_code);
        savedPdf.setFile_url(fileUrl);
        pdfRepository.save(savedPdf);  // URL 정보를 다시 저장

        // 반환을 위한 DTO 설정
        PdfUploadResponseDto responseDto = new PdfUploadResponseDto();
        responseDto.setUser_code(currentUser.getUser_code());
        responseDto.setPdf(new PdfResponseDto(savedPdf));


        //keyword 추출하는 작업 추가예정


        return ResponseEntity.ok(responseDto);
    }


    @Transactional
    public ResponseEntity<?> deletePDFs(List<Integer> pdfIds, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();

        Map<String, String> response = new HashMap<>();

        UserPdfDto userPdfDto = new UserPdfDto(currentUser);

        for (Integer pdfId : pdfIds) {
            boolean PDFExists = userPdfDto.getPdf().stream()
                    .anyMatch(folderDto -> folderDto.getPdf_id().equals(pdfId));
            if (!PDFExists){
                throw new SomethingException("권한이 없습니다. 관리자에게 문의해주세요.");
            }
            Optional<PDF> pdfOptional = pdfRepository.findById(pdfId);
            if (pdfOptional.isPresent()) {
                PDF pdf = pdfOptional.get();
                pdfRepository.delete(pdf);
                s3Uploader.deleteS3(pdf.getFile_url());
            }
        }

        response.put("message", "PDF가 성공적으로 삭제되었습니다.");
        return ResponseEntity.ok(response);

    }



    public ResponseEntity<?> getUserUploadedPdfs(Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();

        UserPdfDto userPdfDto = new UserPdfDto(currentUser);
        return ResponseEntity.ok(userPdfDto);

    }

    @Transactional
    public ResponseEntity<?> updatePDFName(UpdatePdfNameDto updatePdfNameDto, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();
        Map<String, String> response = new HashMap<>();


        UserPdfDto userPdfDto = new UserPdfDto(currentUser);

        boolean pdfNameExists = userPdfDto.getPdf().stream()
                .anyMatch(pdfDto -> pdfDto.getFile_name().equals(updatePdfNameDto.getPdf_name()));

        if (pdfNameExists) {
            throw new SomethingException("중복된 파일이 있습니다.");
        }

        boolean PDFExists = userPdfDto.getPdf().stream()
                .anyMatch(folderDto -> folderDto.getPdf_id().equals(updatePdfNameDto.getPdfId()));
        if (!PDFExists){
            throw new SomethingException("권한이 없습니다. 관리자에게 문의해주세요.");
        }
        Optional<PDF> pdfOptional = pdfRepository.findById(updatePdfNameDto.getPdfId());

        PDF pdf = pdfOptional.get();
        pdf.setFile_name(updatePdfNameDto.getPdf_name());
        pdfRepository.save(pdf);
        response.put("message", "정상적으로 변경되었습니다.");
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<?> updatePDFSubject(UpdatePdfSubjectDto updatePdfSubjectDto, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();
        Map<String, String> response = new HashMap<>();

        UserPdfDto userPdfDto = new UserPdfDto(currentUser);

        boolean PDFExists = userPdfDto.getPdf().stream()
                .anyMatch(folderDto -> folderDto.getPdf_id().equals(updatePdfSubjectDto.getPdfId()));
        if (!PDFExists){
            throw new SomethingException("권한이 없습니다. 관리자에게 문의해주세요.");
        }
        Optional<PDF> pdfOptional = pdfRepository.findById(updatePdfSubjectDto.getPdfId());

        PDF pdf = pdfOptional.get();
        pdf.setSubject(updatePdfSubjectDto.getPdf_subject());
        pdfRepository.save(pdf);
        response.put("message", "정상적으로 변경되었습니다.");
        return ResponseEntity.ok(response);

    }
    @Transactional
    public ResponseEntity<?> getPdfUrl(Integer pdfId, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (!tempUser.isPresent()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();

        Map<String, String> response = new HashMap<>();

        UserPdfDto userPdfDto = new UserPdfDto(currentUser);

        boolean PDFExists = userPdfDto.getPdf().stream()
                .anyMatch(folderDto -> folderDto.getPdf_id().equals(pdfId));
        if (!PDFExists){
            throw new SomethingException("권한이 없습니다. 관리자에게 문의해주세요.");
        }
        Optional<PDF> pdfOptional = pdfRepository.findById(pdfId);
        PDF pdf = pdfOptional.get();

        String objectKey = s3Uploader.extractS3KeyFromUrl(pdf.getFile_url());
        URL presignedUrl = s3Uploader.getPresignedUrl(objectKey, 30);
        response.put("pdf_id",pdfId.toString());
        response.put("presignedUrl", presignedUrl.toString());
        return ResponseEntity.ok(response);
    }
    public Optional<User> findUser(Integer user_code) {
        return userRepository.findById(user_code);
    }

}