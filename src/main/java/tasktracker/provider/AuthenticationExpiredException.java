package tasktracker.provider;

/**
 * Señaliza que la credencial de Google (refresh token) expiró o fue revocada y
 * que el usuario debe volver a autenticarse.
 */
public class AuthenticationExpiredException extends ProviderException {

    public AuthenticationExpiredException(String message) {
        super(message);
    }

    public AuthenticationExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
