package com.classgram.backend.file;

import com.classgram.backend.controller.FileController;
import com.classgram.backend.model.FileGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class FileOperationsService {

	private static final Logger log = LoggerFactory.getLogger(FileOperationsService.class);

	public void deleteLocalFile(String fileName, Path folder) {
		log.info("Deleting local temp file '{}'", Paths.get(folder.toString(), fileName));
		// Deleting stored file...
		try {
			Path path = Paths.get(folder.toString(), fileName);
			Files.delete(path);
			log.info("Local temp file '{}' successfully deleted", Paths.get(folder.toString(), fileName));
		} catch (NoSuchFileException x) {
			log.error("No such file '{}' or directory '{}'", fileName, Paths.get(folder.toString()));
		} catch (DirectoryNotEmptyException x) {
			log.error("Directory '{}' not empty", Paths.get(folder.toString()));
		} catch (IOException x) {
			// File permission problems are caught here
			log.error("Permission error: {}", x.toString());
		}
	}

	// Deletes all the real locally stored files given a list of FileGroups
	public void recursiveLocallyStoredFileDeletion(List<FileGroup> fileGroup) {
		log.info("Recursive deletion of all files in children filegroups");
		if (fileGroup != null) {
			for (FileGroup fg : fileGroup) {
				for (com.classgram.backend.model.File f : fg.getFiles()) {
					this.deleteLocalFile(f.getNameIdent(), FileController.FILES_FOLDER);
				}
				this.recursiveLocallyStoredFileDeletion(fg.getFileGroups());
			}
		}
		return;
	}
	
	public String getFileNameFromURL(String url){
		return (url.substring(url.lastIndexOf('/') + 1));
	}
	
	private String getFileExtension(String name) {
		return name.substring(name.lastIndexOf('.') + 1);
	}

	public String getEncodedPictureName(String originalFileName) {
		// Getting the image extension
		String picExtension = this.getFileExtension(originalFileName);
		// Appending a random integer to the name
		originalFileName += (Math.random() * (Integer.MIN_VALUE - Integer.MAX_VALUE));
		// Encoding original file name + random integer
		originalFileName = new BCryptPasswordEncoder().encode(originalFileName);
		// Deleting all non alphanumeric characters
		originalFileName = originalFileName.replaceAll("[^A-Za-z0-9\\$]", "");
		// Adding the extension
		originalFileName += "." + picExtension;
		return originalFileName;
	}

}
