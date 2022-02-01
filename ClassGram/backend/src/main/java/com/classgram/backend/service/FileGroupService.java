package com.classgram.backend.service;

import com.classgram.backend.model.File;
import com.classgram.backend.model.FileGroup;
import com.classgram.backend.repo.FileGroupRepository;
import com.classgram.backend.struct.FTService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
public class FileGroupService implements FTService<FileGroup, Long> {

    private final FileGroupRepository repo;
    private final FileService fileService;


    @Autowired
    public FileGroupService(FileGroupRepository repo, FileService fileService) {
        this.repo = repo;
        this.fileService = fileService;
    }

    public FileGroup addWebLink(FileGroup fileGroup, File file) {
        File created = fileService.save(file);
        log.info("File saved!");
        fileGroup.getFiles().add(created);
        return this.save(fileGroup);
    }
}
