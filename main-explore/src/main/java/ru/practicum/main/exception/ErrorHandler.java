package ru.practicum.main.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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

    // WARNING: This handler exposes stack traces in the API response.
    // This is for debugging purposes only because server logs are inaccessible to the user.
    // REMOVE THIS HANDLER OR MODIFY IT TO NOT INCLUDE STACK TRACES IN PRODUCTION.

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        // You might want to log the full exception on the server side as well
        // log.error("Unhandled exception: {}", e.getMessage(), e);
        List<String> stackTrace = List.of(e.getClass().getName() + ": " + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            stackTrace = new java.util.ArrayList<>(stackTrace); // Make it mutable
            stackTrace.add(element.toString());
        }

        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                e.getMessage(),
                stackTrace,
                LocalDateTime.now()
        );
    }

}



    