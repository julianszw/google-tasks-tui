package tasktracker.google;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GoogleAuthTest {

    @TempDir
    Path tempDir;

    @Test
    void signOutDeletesTokenStore() throws IOException {
        Path tokenDir = tempDir.resolve("google-tokens");
        Files.createDirectories(tokenDir);
        Files.writeString(tokenDir.resolve("StoredCredential"), "{}");
        GoogleAuth auth = new GoogleAuth(tempDir);

        auth.signOut();

        assertFalse(Files.exists(tokenDir));
    }

    @Test
    void signOutWithoutTokenStoreDoesNothing() {
        GoogleAuth auth = new GoogleAuth(tempDir);

        auth.signOut();

        assertFalse(Files.exists(tempDir.resolve("google-tokens")));
    }
}
