package com.innovationhub.backend.exception;

public class AccountInfoConflictException extends RuntimeException {
    public AccountInfoConflictException(String message) {
        super(message);
    }
}
