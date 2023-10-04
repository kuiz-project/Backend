package com.kuiz.demo.repository;

import com.kuiz.demo.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test, Integer> {
}
