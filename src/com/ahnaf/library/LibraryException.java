package com.ahnaf.library;

/** Thrown when a library operation is not allowed, e.g. borrowing a book that is already out. */
public class LibraryException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LibraryException(String message) {
        super(message);
    }
}
