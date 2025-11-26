package ru.practicum.main.exception;

import org.springframework.dao.DataIntegrityViolationException as SpringDataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        return new ApiError(
                HttpStatus.NOT_FOUND,
                "The required object was not found.",
                e.getMessage(),
                Collections.emptyList(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        return new ApiError(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                e.getMessage(),
                Collections.emptyList(),
                LocalDateTime.now()
        );
    }
    
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleSpringDataIntegrityViolationException(final SpringDataIntegrityViolationException e) {
        return new ApiError(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                e.getMessage(),
                Collections.emptyList(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage(),
                Collections.emptyList(),
                LocalDateTime.now()
        );
    }
}
