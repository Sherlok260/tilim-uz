package com.example.springtilimuzxml.controller;

import com.example.springtilimuzxml.payload.ApiResponse;
import com.example.springtilimuzxml.service.LatinToKirilService;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.IOException;

@RequestMapping("/api")
@RestController
public class LatinToKirilController {

    @Autowired
    LatinToKirilService latinToKirilService;

    @PostMapping("/file")
    public HttpEntity<?> file(@RequestParam("file")MultipartFile file) throws JAXBException, IOException, Docx4JException {
        ApiResponse apiResponse = latinToKirilService.translator(file);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/hello")
    public HttpEntity<?> hello() {
        return ResponseEntity.ok("hello");
    }

}
