package nl.tudelft.sem.template.example.exception;

public class CourierNotFoundException extends Exception {
    public CourierNotFoundException(String message) {
        super(message);
    }
}