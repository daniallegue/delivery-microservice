package nl.tudelft.sem.template.example.exception;

public class RatingNotFoundException extends Exception {
    public RatingNotFoundException(String message) {
        super(message);
    }
}
