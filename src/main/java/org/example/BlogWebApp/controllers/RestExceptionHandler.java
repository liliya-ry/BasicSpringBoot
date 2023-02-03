package org.example.BlogWebApp.controllers;

import org.slf4j.*;

//@ControllerAdvice
public class RestExceptionHandler { // extends ResponseEntityExceptionHandler {
    private static final Logger EXCEPTION_LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);
//
//    @ExceptionHandler({NotFoundException.class})
//    protected ResponseEntity<Object> handleNotFound(Exception ex, WebRequest request) {
//        EXCEPTION_LOGGER.warn("Exception: {}", ex.toString());
//        return handleExceptionInternal(ex, ex.getMessage(),
//                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
//    }
}
