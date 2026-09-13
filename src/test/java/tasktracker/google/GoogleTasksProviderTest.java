package tasktracker.google;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import tasktracker.provider.AuthenticationExpiredException;
import tasktracker.provider.ProviderException;

class GoogleTasksProviderTest {

    @Test
    void toProviderExceptionDetectsInvalidGrantFromTokenResponseException() throws IOException {
        ProviderException result = GoogleTasksProvider.toProviderException(
                "No se pudieron listar las listas", tokenResponseException("invalid_grant"));

        assertInstanceOf(AuthenticationExpiredException.class, result);
    }

    @Test
    void toProviderExceptionDetectsInvalidGrantFromGoogleJsonResponseException() {
        ProviderException result = GoogleTasksProvider.toProviderException(
                "No se pudieron listar las listas", googleJsonResponseException("invalid_grant"));

        assertInstanceOf(AuthenticationExpiredException.class, result);
    }

    @Test
    void toProviderExceptionDetectsInvalidGrantNestedAsTokenResponseCause() throws IOException {
        GoogleJsonResponseException outer = googleJsonResponseException("authError");
        outer.initCause(tokenResponseException("invalid_grant"));

        ProviderException result = GoogleTasksProvider.toProviderException(
                "No se pudieron listar las listas", outer);

        assertInstanceOf(AuthenticationExpiredException.class, result);
    }

    @Test
    void toProviderExceptionKeepsGenericMessageForUnrelatedTokenError() throws IOException {
        ProviderException result = GoogleTasksProvider.toProviderException(
                "No se pudieron listar las listas", tokenResponseException("invalid_client"));

        assertFalse(result instanceof AuthenticationExpiredException);
        assertTrue(result.getMessage().contains("No se pudieron listar las listas"));
    }

    @Test
    void toProviderExceptionKeepsGenericMessageForUnrelatedJsonError() {
        ProviderException result = GoogleTasksProvider.toProviderException(
                "No se pudieron listar las listas", googleJsonResponseException("rateLimitExceeded"));

        assertFalse(result instanceof AuthenticationExpiredException);
        assertTrue(result.getMessage().contains("No se pudieron listar las listas"));
    }

    @Test
    void toProviderExceptionWithNullTokenDetailsKeepsGenericMessage() throws IOException {
        ProviderException result = GoogleTasksProvider.toProviderException(
                "No se pudieron listar las listas", tokenResponseExceptionWithNullDetails());

        assertFalse(result instanceof AuthenticationExpiredException);
        assertTrue(result.getMessage().contains("No se pudieron listar las listas"));
    }

    @Test
    void toProviderExceptionWithNullJsonDetailsKeepsGenericMessage() {
        GoogleJsonResponseException ex = new GoogleJsonResponseException(
                new HttpResponseException.Builder(400, "Bad Request", new HttpHeaders()), null);

        ProviderException result = GoogleTasksProvider.toProviderException(
                "No se pudieron listar las listas", ex);

        assertFalse(result instanceof AuthenticationExpiredException);
        assertTrue(result.getMessage().contains("No se pudieron listar las listas"));
    }

    @Test
    void toProviderExceptionPreservesOriginalCause() throws IOException {
        IOException original = tokenResponseException("invalid_grant");

        ProviderException result = GoogleTasksProvider.toProviderException(
                "No se pudieron listar las listas", original);

        assertInstanceOf(AuthenticationExpiredException.class, result);
        assertSame(original, result.getCause());
    }

    private static TokenResponseException tokenResponseException(String error) throws IOException {
        MockHttpTransport transport = new MockHttpTransport.Builder()
                .setLowLevelHttpResponse(new MockLowLevelHttpResponse()
                        .setStatusCode(400)
                        .setContentType("application/json")
                        .setContent("{\"error\":\"" + error + "\"}"))
                .build();
        return fromTransport(transport);
    }

    private static TokenResponseException tokenResponseExceptionWithNullDetails() throws IOException {
        MockHttpTransport transport = new MockHttpTransport.Builder()
                .setLowLevelHttpResponse(new MockLowLevelHttpResponse()
                        .setStatusCode(400)
                        .setContent("not-json-content"))
                .build();
        return fromTransport(transport);
    }

    private static TokenResponseException fromTransport(MockHttpTransport transport) throws IOException {
        HttpRequest request = transport.createRequestFactory()
                .buildPostRequest(new GenericUrl("https://oauth2.googleapis.com/token"), null);
        request.setThrowExceptionOnExecuteError(false);
        return TokenResponseException.from(GsonFactory.getDefaultInstance(), request.execute());
    }

    private static GoogleJsonResponseException googleJsonResponseException(String reason) {
        GoogleJsonError.ErrorInfo info = new GoogleJsonError.ErrorInfo();
        info.setReason(reason);
        GoogleJsonError details = new GoogleJsonError();
        details.setErrors(List.of(info));
        return new GoogleJsonResponseException(
                new HttpResponseException.Builder(400, "Bad Request", new HttpHeaders()), details);
    }
}
