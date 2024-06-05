package com.apocaly.apocaly_path_private.test;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Hidden
public class TestController {

    @GetMapping("/test/{test}")
    public ResponseEntity<?> testGetMapping(@PathVariable String test){
        return ResponseEntity.ok(test);
    }
}
