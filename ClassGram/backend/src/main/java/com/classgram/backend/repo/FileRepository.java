package com.classgram.backend.repo;

import com.classgram.backend.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {

    File findById(long id);
}
