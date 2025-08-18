package com.gen.rally.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler{
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorResponse body = ErrorResponse.from(e.getErrorCode());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(body);
    }

    /**
     * DB 제약 위반 등으로 DataIntegrityViolationException이 발생하면
     * PROFILE_IMAGE_TOO_LARGE 같은 별도 ErrorCode를 정의해서 내려줄 수 있습니다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataException(DataIntegrityViolationException e) {
        // 예를 들어 BLOB 크기 초과 같은 상황에 맞는 코드를 추가로 정의
        ErrorResponse body = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * 그 외 모든 예외는 서버 에러로 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
        ErrorResponse body = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
