package com.handson.basic.error;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(Long id) {
        super("User not found: " + id);
    }
}

