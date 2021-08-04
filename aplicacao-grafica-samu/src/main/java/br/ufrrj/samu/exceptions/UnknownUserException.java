package br.ufrrj.samu.exceptions;

public class UnknownUserException extends Exception {
    public UnknownUserException() {
    }

    public UnknownUserException(String message) {
        super(message);
    }

    public UnknownUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownUserException(Throwable cause) {
        super(cause);
    }

    public static class AlreadyExistsException extends Exception {

        public AlreadyExistsException() {
        }

        public AlreadyExistsException(String message) {
            super(message);
        }

        public AlreadyExistsException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
