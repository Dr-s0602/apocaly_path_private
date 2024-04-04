package com.apocaly.apocaly_path_private.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class testController {

    @GetMapping("/test/{message}")
    public ResponseEntity<String> testHi( @PathVariable String message){
        log.info(message);
        return ResponseEntity.ok(message+" hi");
    }
}
