package account.exception;

public class CustomAccessDeniedException extends RuntimeException {
    public CustomAccessDeniedException() {super("Access Denied!");}
}
