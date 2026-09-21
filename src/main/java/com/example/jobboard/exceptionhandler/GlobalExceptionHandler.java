package com.example.jobboard.exceptionhandler;

import com.example.jobboard.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    // not found exceptions
    @ExceptionHandler({JobNotFoundException.class,
            ApplicationNotFoundException.class,
            UserNotFoundException.class,
            ResourceNotFoundException.class})
    public ModelAndView handleResourceNotFoundException(Exception e, HttpServletRequest request) {
        return buildErrorPage(e, HttpStatus.NOT_FOUND, request);
    }

    // access denied exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(Exception e, HttpServletRequest request) {
        return buildErrorPage(e, HttpStatus.FORBIDDEN, request);
    }

    // conflict exceptions
    @ExceptionHandler(EmailAlreadyExistException.class)
    public ModelAndView handleEmailAlreadyExistException(Exception e, HttpServletRequest request) {
        return buildErrorPage(e, HttpStatus.CONFLICT, request);
    }

    // bad request exceptions
    @ExceptionHandler(BadRequestException.class)
    public ModelAndView handleBadRequestException(Exception e, HttpServletRequest request) {
        return buildErrorPage(e, HttpStatus.BAD_REQUEST, request);
    }

    private ModelAndView buildErrorPage(Exception e,HttpStatus status,HttpServletRequest request) {
        ModelAndView page = new ModelAndView("error");
        page.addObject("message", e.getMessage());
        page.addObject("status", status.value());
        page.addObject("path", request.getRequestURI());
        page.addObject("error", status.getReasonPhrase());
        page.addObject("timestamp", LocalDateTime.now());
        return page;
    }

}
