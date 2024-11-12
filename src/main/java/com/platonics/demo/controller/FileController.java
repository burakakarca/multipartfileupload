package com.platonics.demo.controller;

import com.platonics.demo.model.ResponseValidateFile;
import com.platonics.demo.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) throws Exception {
        ResponseValidateFile responseValidateFile = fileService.validateFile(file);
        if (CollectionUtils.isEmpty(responseValidateFile.getErrorList())) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseValidateFile.getErrorList());
        }
    }
}
