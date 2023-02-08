package org.example.SpringFramework.SpringBoot.servlet.errors;

public class ServerError {
    private static  final String DEFAULT_ERROR = "Internal Server Error";
    private static final int DEFAULT_STATUS = 500;

    private long timeStamp;
    private int status;
    private String error;
    private String path;

    public ServerError(int status, String error, String path) {
        this.timeStamp = System.currentTimeMillis();
        this.status = status;
        this.error = error;
        this.path = path;
    }

    public ServerError(String path) {
        this(DEFAULT_STATUS, DEFAULT_ERROR, path);
    }
}
