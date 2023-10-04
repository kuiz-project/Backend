package com.kuiz.demo.repository;

import com.kuiz.demo.model.Folder;
import com.kuiz.demo.model.PDF;
import com.kuiz.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Integer> {
}
