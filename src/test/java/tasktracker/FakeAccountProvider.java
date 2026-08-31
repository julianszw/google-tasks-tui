package tasktracker;

import tasktracker.provider.AccountProvider;

public class FakeAccountProvider implements AccountProvider {

    private String email;
    private boolean signedOut;
    private int authorizeCalls;

    @Override
    public String accountEmail() {
        return email;
    }

    @Override
    public void authorize() {
        authorizeCalls++;
    }

    @Override
    public void signOut() {
        signedOut = true;
        email = null;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSignedOut() {
        return signedOut;
    }

    public int authorizeCalls() {
        return authorizeCalls;
    }
}
