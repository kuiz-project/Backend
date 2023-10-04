package com.kuiz.demo.repository;

import com.kuiz.demo.model.PDF;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PDFRepository extends JpaRepository<PDF, Integer> {
}
