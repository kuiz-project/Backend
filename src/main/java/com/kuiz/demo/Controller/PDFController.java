package com.kuiz.demo.Controller;

import com.kuiz.demo.Dto.UpdatePdfNameDto;
import com.kuiz.demo.Dto.UpdatePdfSubjectDto;
import com.kuiz.demo.service.PDFService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/pdf")
public class PDFController {

    @Autowired
    private PDFService pdfService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPDF(@RequestParam("file") MultipartFile file,
                                       @RequestParam("subject") String subject,
                                       HttpSession session) {
        Integer currentUser = (Integer) session.getAttribute("user");

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인하지 않았습니다.");
        }

        return pdfService.uploadPDF(file, subject, currentUser);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deletePDFs(@RequestParam List<Integer> pdfIds, HttpSession session) {
        Integer currentUser = (Integer) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "로그인하지 않았습니다."));
        }
        return pdfService.deletePDFs(pdfIds, currentUser);
    }

    @GetMapping("/my-pdfs")
    public ResponseEntity<?> getMyUploadedPdfs(HttpSession session) {
        Integer user_code = (Integer) session.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
        return pdfService.getUserUploadedPdfs(user_code);
    }

    @PatchMapping("/update-name")
    public ResponseEntity<?> updatePDFName(@RequestBody UpdatePdfNameDto updatePdfNameDto,
                                           HttpSession session) {
        Integer user_code = (Integer) session.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }

        return pdfService.updatePDFName(updatePdfNameDto, user_code);
    }

    @PatchMapping("/update-subject")
    public ResponseEntity<?> updatePDFSubject(@RequestBody UpdatePdfSubjectDto updatePdfSubjectDto,
                                              HttpSession session) {
        Integer user_code = (Integer) session.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }

        return pdfService.updatePDFSubject(updatePdfSubjectDto, user_code);
    }

    @GetMapping("/getpdfurl/{pdfId}")
    public ResponseEntity<?> getPdfUrl(@PathVariable Integer pdfId,HttpSession session) {
        Integer user_code = (Integer) session.getAttribute("user");
        if (user_code == null) {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("error", "로그인하지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
        return pdfService.getPdfUrl(pdfId, user_code);
    }


}
