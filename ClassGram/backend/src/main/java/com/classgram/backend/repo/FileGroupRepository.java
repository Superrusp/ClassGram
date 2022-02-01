package com.classgram.backend.repo;

import com.classgram.backend.model.FileGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileGroupRepository extends JpaRepository<FileGroup, Long> {


    FileGroup findById(long id);


}
