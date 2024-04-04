package com.apocaly.apocaly_path_private.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ResponseStatusException 타입의 예외를 처리합니다.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        // 클라이언트에게 에러의 상태 코드와 메시지를 반환합니다.
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }

    // DataAccessException 타입의 예외를 처리합니다. (예를 들어 데이터베이스 에러)
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
        // 내부 서버 에러 상태 코드와 함께 클라이언트에게 메시지를 반환합니다.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("데이터베이스 처리 중 오류가 발생했습니다.");
    }

    // 다른 모든 예외 타입을 처리합니다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        // 클라이언트에게 일반적인 에러 메시지를 반환합니다.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버에서 오류가 발생했습니다.");
    }
}
