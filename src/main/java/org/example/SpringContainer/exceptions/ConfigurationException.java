package org.example.SpringContainer.exceptions;

public class ConfigurationException extends Exception {
    private final String message;

    public ConfigurationException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
