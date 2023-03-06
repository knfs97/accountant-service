package account.exception;


public class EmailTakenException extends RuntimeException {
    public EmailTakenException() {super("User exist!");}
}