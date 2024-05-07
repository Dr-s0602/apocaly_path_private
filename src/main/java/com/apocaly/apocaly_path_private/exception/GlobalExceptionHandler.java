package com.apocaly.apocaly_path_private.exception;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
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

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        // ResponseStatusException 예외를 처리합니다. 이 예외는 HTTP 상태 코드와 관련된 예외를 나타냅니다.
        // ex.getReason()은 예외 발생 시 제공되는 상세 메시지를, ex.getStatusCode()는 HTTP 상태 코드를 반환합니다.
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
        // 데이터베이스 접근 중 발생하는 모든 예외를 처리합니다. 주로 CRUD 작업 실패 시 발생합니다.
        // HttpStatus.INTERNAL_SERVER_ERROR로 설정해 내부 서버 오류임을 알립니다.
        // 반환 메시지는 데이터베이스 오류 발생을 사용자에게 알리는 데 사용됩니다.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("데이터베이스 처리 중 오류가 발생했습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        // 기타 모든 예외의 상위 클래스인 Exception을 처리합니다.
        // 이 메서드는 처리되지 않은 모든 예외를 캐치하며, 일반적인 서버 내부 오류 메시지를 반환합니다.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버에서 오류가 발생했습니다.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 메서드의 인자값이 유효하지 않을 때 발생하는 예외를 처리합니다.
        // 주로 @Valid 어노테이션이 사용된 객체의 검증 과정에서 문제가 발생했을 때 이 예외가 발생합니다.
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField(); // 오류가 발생한 필드 이름
            String errorMessage = error.getDefaultMessage(); // 오류 메시지
            errors.put(fieldName, errorMessage); // 필드 이름과 오류 메시지를 맵에 추가
        });
        // 유효성 검사 실패에 대한 상세 정보를 담은 맵을 클라이언트에 반환합니다.
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException ex) {
        log.info("예외 처리 해야함");
        // 클라이언트에게 토큰 만료 메시지와 함께 401 상태 코드를 반환합니다.
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Session has expired. Please log in again.");
    }
}
