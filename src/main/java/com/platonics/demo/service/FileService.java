package com.platonics.demo.service;

import com.platonics.demo.model.ResponseValidateFile;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    ResponseValidateFile validateFile(MultipartFile file) throws Exception;
}
