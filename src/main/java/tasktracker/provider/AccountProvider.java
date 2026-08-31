package tasktracker.provider;

public interface AccountProvider {

    String accountEmail();

    void authorize();

    void signOut();
}
