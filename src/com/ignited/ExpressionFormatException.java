package com.ignited;

public class ExpressionFormatException extends IllegalArgumentException {

    public ExpressionFormatException() {
    }

    public ExpressionFormatException(String s) {
        super(s);
    }

    public ExpressionFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
