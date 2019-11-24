package io.github.patrickconley.arbutus.scanner;

public class ScannerException extends Exception {

    public ScannerException(Exception cause) {
        super(cause);
    }

    public ScannerException(String message) {
        super(message);
    }

    public ScannerException(String message, IllegalArgumentException cause) {
        super(message, cause);
    }
}
