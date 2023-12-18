package nl.tudelft.sem.template.example.exception;

public class NoAvailableOrdersException extends Exception {
    public NoAvailableOrdersException(String message) {
        super(message);
    }
}
