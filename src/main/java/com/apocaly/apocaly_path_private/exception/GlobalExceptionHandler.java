package com.apocaly.apocaly_path_private.exception;

// Spring Framework의 웹 애플리케이션에서 발생하는 예외를 전역적으로 처리하는 클래스입니다.
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice 어노테이션은 이 클래스가 예외 처리기로 동작하게 하며,
// 모든 @RestController에서 발생하는 예외를 이곳에서 처리할 수 있게 합니다.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ResponseStatusException 타입의 예외를 처리하는 메서드입니다.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        // 예외가 발생했을 때, 예외의 상태 코드와 이유를 클라이언트에 반환합니다.
        // 예를 들어, 404 Not Found나 400 Bad Request 등의 상태를 처리할 때 사용됩니다.
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }

    // DataAccessException 타입의 예외를 처리하는 메서드입니다.
    // 이 타입의 예외는 주로 데이터베이스 작업 중에 발생하는 예외를 처리하기 위해 사용됩니다.
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
        // 데이터베이스 작업 중 예외가 발생했을 때, 내부 서버 오류(500) 상태 코드와
        // 사용자에게 친숙한 메시지를 반환하여 데이터베이스 오류가 발생했음을 알립니다.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("데이터베이스 처리 중 오류가 발생했습니다.");
    }

    // Exception 클래스 타입의 예외를 처리하는 메서드입니다.
    // 이 메서드는 앞서 정의한 특정 타입의 예외를 제외한 모든 예외를 처리합니다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        // 알 수 없는 예외가 발생했을 때, 내부 서버 오류(500) 상태 코드와
        // 일반적인 에러 메시지를 반환하여 서버 내부의 문제가 발생했음을 알립니다.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버에서 오류가 발생했습니다.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
