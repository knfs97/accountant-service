package account.exception;

import javax.naming.AuthenticationException;

public class UserLockedException extends AuthenticationException {
    public UserLockedException(String message) {
        super(message);
    }
}
