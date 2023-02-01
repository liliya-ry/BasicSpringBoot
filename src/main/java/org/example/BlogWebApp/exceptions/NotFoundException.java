package org.example.BlogWebApp.exceptions;

public class NotFoundException extends RuntimeException {
    private final String message;

    public NotFoundException(String jsonMessage) {
        message = jsonMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
