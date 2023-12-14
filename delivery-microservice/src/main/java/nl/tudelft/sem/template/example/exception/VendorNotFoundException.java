package nl.tudelft.sem.template.example.exception;

public class VendorNotFoundException extends Exception {
    public VendorNotFoundException(String message) {
        super(message);
    }
}