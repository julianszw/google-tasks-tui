package tasktracker.provider;

/**
 * Fake de {@link AccountProvider} para tests del paquete {@code tasktracker.provider},
 * sin depender del fixture del paquete {@code tasktracker}.
 */
class FakeAccountProvider implements AccountProvider {

    private boolean signedOut;
    private int authorizeCalls;

    @Override
    public String accountEmail() {
        return null;
    }

    @Override
    public void authorize() {
        authorizeCalls++;
    }

    @Override
    public void signOut() {
        signedOut = true;
    }

    boolean isSignedOut() {
        return signedOut;
    }

    int authorizeCalls() {
        return authorizeCalls;
    }
}
