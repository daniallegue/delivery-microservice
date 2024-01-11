package nl.tudelft.sem.template.example.exception;

public class MicroserviceCommunicationException extends Exception {
    public MicroserviceCommunicationException(String message) {
        super(message);
    }
}
