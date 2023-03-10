package entities;

public class ErrorResponse {
    public static final String NO_POST_MESSAGE = "No post with postId ";
    public static final String NO_USER_MESSAGE = "No post with username ";
    private int code;
    private String message;

    public ErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "{\n" +
                "   \"code\":" + code + ",\n" +
                "   \"message\":\"" + message + "\"\n" +
                '}';
    }
}
