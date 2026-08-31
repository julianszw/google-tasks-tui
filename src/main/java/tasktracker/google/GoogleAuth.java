package tasktracker.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.tasks.TasksScopes;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import tasktracker.provider.AccountProvider;
import tasktracker.provider.ProviderException;

public final class GoogleAuth implements AccountProvider {

    private static final String DATA_STORE_DIR = "google-tokens";
    private static final String USERINFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
    private static final String USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final Path workingDir;
    private volatile GoogleAuthorizationCodeFlow flow;
    private volatile String cachedEmail;

    public GoogleAuth(Path workingDir) {
        this.workingDir = workingDir;
    }

    public boolean hasStoredCredentials() {
        try {
            return flow().loadCredential("user") != null;
        } catch (IOException | GeneralSecurityException e) {
            throw new ProviderException("No se pudo verificar la sesión guardada: " + e.getMessage(), e);
        }
    }

    public Credential loadCredential() {
        try {
            return flow().loadCredential("user");
        } catch (IOException | GeneralSecurityException e) {
            throw new ProviderException("No se pudo cargar la sesión de Google: " + e.getMessage(), e);
        }
    }

    @Override
    public void authorize() {
        try {
            GoogleAuthorizationCodeFlow flow = flow();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().build();
            AuthorizationCodeInstalledApp.Browser browser = url -> {
                System.out.println();
                System.out.println("Abrí esta URL en tu navegador para autenticarte:");
                System.out.println(url);
                System.out.println();
                try {
                    new ProcessBuilder("xdg-open", url)
                            .redirectErrorStream(true)
                            .start();
                } catch (Exception ignored) {
                    // xdg-open no disponible; el usuario usará la URL impresa
                }
            };
            new AuthorizationCodeInstalledApp(flow, receiver, browser).authorize("user");
            cachedEmail = null;
        } catch (Exception e) {
            throw new ProviderException("No se pudo autenticar con Google: " + e.getMessage(), e);
        }
    }

    @Override
    public String accountEmail() {
        String email = cachedEmail;
        if (email != null) {
            return email;
        }
        Credential credential = loadCredential();
        if (credential == null) {
            return null;
        }
        try {
            if (credential.getAccessToken() == null) {
                credential.refreshToken();
            }
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
            HttpResponse response = requestFactory.buildGetRequest(new GenericUrl(USERINFO_ENDPOINT)).execute();
            try {
                JsonObjectParser parser = new JsonObjectParser(GsonFactory.getDefaultInstance());
                Map<String, Object> info = parser.parseAndClose(
                        response.getContent(), StandardCharsets.UTF_8, Map.class);
                email = info == null ? null : (String) info.get("email");
            } finally {
                response.disconnect();
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new ProviderException("No se pudo obtener el email de la cuenta: " + e.getMessage(), e);
        }
        cachedEmail = email;
        return email;
    }

    @Override
    public void signOut() {
        cachedEmail = null;
        Path dir = workingDir.resolve(DATA_STORE_DIR);
        if (!Files.exists(dir)) {
            return;
        }
        try (var paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // best effort
                }
            });
        } catch (IOException ignored) {
            // best effort
        }
    }

    private GoogleAuthorizationCodeFlow flow() throws IOException, GeneralSecurityException {
        GoogleAuthorizationCodeFlow f = flow;
        if (f == null) {
            synchronized (this) {
                if (flow == null) {
                    flow = buildFlow();
                }
                f = flow;
            }
        }
        return f;
    }

    private GoogleAuthorizationCodeFlow buildFlow() throws IOException, GeneralSecurityException {
        GoogleClientSecrets secrets = loadClientSecrets();
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        FileDataStoreFactory store = new FileDataStoreFactory(workingDir.resolve(DATA_STORE_DIR).toFile());
        return new GoogleAuthorizationCodeFlow.Builder(
                transport, GsonFactory.getDefaultInstance(), secrets,
                List.of(TasksScopes.TASKS, USERINFO_EMAIL_SCOPE))
                .setDataStoreFactory(store)
                .setAccessType("offline")
                .build();
    }

    private GoogleClientSecrets loadClientSecrets() throws IOException {
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        if (clientId != null && clientSecret != null) {
            return new GoogleClientSecrets()
                    .setInstalled(new GoogleClientSecrets.Details()
                            .setClientId(clientId.trim())
                            .setClientSecret(clientSecret.trim()));
        }
        Path file = workingDir.resolve("credentials.json");
        if (Files.exists(file)) {
            return GoogleClientSecrets.load(GsonFactory.getDefaultInstance(), new FileReader(file.toFile()));
        }
        throw new ProviderException("No se encontraron credenciales de Google "
                + "(define GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET o crea credentials.json)");
    }
}
