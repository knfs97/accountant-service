package account.exception;

public class MalformedEmailException extends RuntimeException {
    public MalformedEmailException() {super("Malformed email, it does not end with <acme.com>");}
}