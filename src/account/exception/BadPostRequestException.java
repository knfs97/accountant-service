package account.exception;

public class BadPostRequestException extends RuntimeException {
    public BadPostRequestException() {super("Missing elements in post request");}
}
