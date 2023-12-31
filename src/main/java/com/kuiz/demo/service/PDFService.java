package com.kuiz.demo.service;

import java.io.*;

import com.kuiz.demo.Dto.*;
import com.kuiz.demo.exception.SomethingException;
import com.kuiz.demo.model.*;
import com.kuiz.demo.repository.FolderRepository;
import com.kuiz.demo.repository.PDFRepository;
import com.kuiz.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.net.URL;
import java.util.*;

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


    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> uploadPDF(MultipartFile multipartFile, Subject subject,Integer folder_id, Integer user_code) {
        Optional<User> tempUser = findUser(user_code);
        if (tempUser.isEmpty()){
            throw new SomethingException("잘못된 접근입니다.");
        }
        User currentUser = tempUser.get();

        Optional<Folder> tempfolder = folderRepository.findById(folder_id);

        if (tempfolder.isEmpty()){
            throw new SomethingException("folder id의 폴더를 찾지 못함");
        }

        Folder targetfolder = tempfolder.get();

        // 먼저 PDF 정보를 데이터베이스에 저장


        String fileUrl = "";
        File tempFile = null;
        try {
            PDF pdf = PDF.builder()
                    .file_name(multipartFile.getOriginalFilename())
                    .subject(subject)
                    .folder(targetfolder)
                    .user(currentUser)
                    .build();
            PDF savedPdf = pdfRepository.save(pdf);
            fileUrl = s3Uploader.uploadFileToS3(multipartFile, user_code);

            savedPdf.setFile_url(fileUrl);
            // PDF 파일을 임시 파일로 저장
            try {
                tempFile = File.createTempFile("uploadedPdf", ".pdf");
                multipartFile.transferTo(tempFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //keyword 추출하는 작업 추가

            CreateKeywordsDto createKeywordsDto = new CreateKeywordsDto();
            createKeywordsDto.setPdf_url(tempFile.getAbsolutePath());
            createKeywordsDto.setSubject(subject);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString;
            try {
                jsonString = objectMapper.writeValueAsString(createKeywordsDto);
            } catch (Exception e) {
                throw new RuntimeException("Error serializing createQuestionDto", e);
            }

            String pythonPath = "/home/ubuntu/python/get_keyword.py";

            String pythonOutput = executePythonScript(pythonPath, jsonString);

            Map<String, List<String>> tempResult;
            try {
                tempResult = objectMapper.readValue(pythonOutput, new TypeReference<Map<String, List<String>>>() {});
            } catch (Exception e) {
                throw new RuntimeException("파이썬 키워드 추출 후 error",e);
            }

            Keywords keywords = new Keywords();
            keywords.setPageKeywords(tempResult);

            savedPdf.setKeywords(keywords);
            pdfRepository.save(savedPdf);

            // 파이썬 스크립트 실행 후 임시 파일 삭제
            if(tempFile.exists()) {
                tempFile.delete();
            }
            System.out.println("pdf 과목 : "+pdf.getSubject()+"\n+추출 keyword:"+keywords.getPageKeywords());


            PdfDto pdfDto = new PdfDto(savedPdf);

            //Map<String, Object> response = new HashMap<>();
            //response.put("message2", String.valueOf(keywords));

            return ResponseEntity.ok(pdfDto);
        } catch (RuntimeException e) {
            // 중간에 에러 발생 시, 업로드된 파일 삭제
            if (!fileUrl.isEmpty()) {
                s3Uploader.deleteS3(fileUrl);
            }

            // 에러 메시지 전달
            Map<String, String> response = new HashMap<>();
            response.put("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

        } finally {
            // 키워드 추출 후 임시 파일 삭제 로직
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        // 데이터베이스 저장 후 S3에 파일 업로드

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

        // First loop: Validate the PDFs
        for (Integer pdfId : pdfIds) {
            boolean PDFExists = userPdfDto.getPdf().stream()
                    .anyMatch(folderDto -> folderDto.getPdf_id().equals(pdfId));
            if (!PDFExists){
                throw new SomethingException("권한이 없습니다. 관리자에게 문의해주세요.");
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

    private String executePythonScript(String scriptPath, String jsonString) {
        ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath);
        processBuilder.redirectErrorStream(true);
        StringBuilder output = new StringBuilder();

        try {
            Process process = processBuilder.start();
            try (
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
            ) {
                // jsonString을 파이프를 통해 전달
                writer.write(jsonString);
                writer.flush();
                writer.close();

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                    output.append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("파이썬 스크립트가 다음 코드로 종료되었습니다: " + exitCode + " 출력:\n" + output.toString());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("파이썬 스크립트 실행 중 오류 발생", e);
        }

        return output.toString().trim();

    }
}