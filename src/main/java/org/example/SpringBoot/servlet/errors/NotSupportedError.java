package org.example.SpringBoot.servlet.errors;

public class NotSupportedError {
    private static  final String DEFAULT_TITLE = "Method Not Allowed";
    private static final int DEFAULT_STATUS = 405;
    private static final String DEFAULT_TYPE = "about:blank";

    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private String properties;

    public NotSupportedError(String type, String title, int status, String method, String instance, String properties) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = "Method '" + method + "' is not supported.";
        this.instance = instance;
        this.properties = properties;
    }

    public NotSupportedError(String method, String instance) {
        this(DEFAULT_TYPE, DEFAULT_TITLE, DEFAULT_STATUS, method, instance, null);
    }
}
