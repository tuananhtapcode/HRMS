package com.project.hrms.exception;
public class PermissionDenyException extends RuntimeException {
    public PermissionDenyException() {
        super("Permission denied");
    }
    public PermissionDenyException(String message) {
        super(message);
    }
}
