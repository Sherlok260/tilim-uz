package com.example.springtilimuzxml.controller;

import com.example.springtilimuzxml.payload.ApiResponse;
import com.example.springtilimuzxml.service.LatinToKirilService;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.python.google.common.io.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    @GetMapping(value = "/getfile", produces = { "application/octet-stream" })
    public ResponseEntity<byte[]> download() {

        try {

            File file = new File(Resources.getResource("uploads/Result.docx").getPath());
            String absolutePath = file.getAbsolutePath();
            System.out.println(absolutePath);
            byte[] contents = Files.readAllBytes(Paths.get(absolutePath));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename("Result.docx").build());

            return new ResponseEntity<>(contents, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/hello")
    public HttpEntity<?> hello() {
        return ResponseEntity.ok("hello");
    }

}
