package br.com.minispring;

import br.com.minispring.framework.MiniApplication;
import br.com.minispring.framework.context.AppConfig;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;

class ApiIntegrationTest {
    private MiniApplication app;
    private HttpClient client;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach void start() throws Exception {
        var properties = new Properties();
        properties.setProperty("app.name", "Catálogo de teste");
        app = MiniApplication.start("br.com.minispring.filmes", new AppConfig(properties, Map.of()), 0);
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }
    @AfterEach void stop() throws Exception {
        if (client != null) client.close();
        if (app != null) app.close();
    }
    private HttpRequest request(String method, String path, String body, String contentType) {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + app.port() + path)).timeout(Duration.ofSeconds(10));
        if (contentType != null) builder.header("Content-Type", contentType);
        return builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build();
    }
    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        return client.send(request(method, path, body, body == null ? null : "application/json"), HttpResponse.BodyHandlers.ofString());
    }
    private static String filme(String titulo, Object duracao) {
        return "{\"titulo\":" + (titulo == null ? "null" : "\"" + titulo + "\"") + ",\"duracaoMinutos\":" + duracao + "}";
    }
    private JsonNode get(String path) throws Exception { return json.readTree(send("GET", path, null).body()); }

    @Test void fullCrudThroughRealJetty() throws Exception {
        assertEquals("Catálogo de teste", get("/").get("application").asText());
        assertEquals(200, send("GET", "/health", null).statusCode());
        assertEquals(0, get("/filmes").size());

        var created = send("POST", "/filmes", filme(" Matrix ", 136));
        assertEquals(201, created.statusCode());
        assertTrue(created.headers().firstValue("Content-Type").orElseThrow().startsWith("application/json"));
        String location = created.headers().firstValue("Location").orElseThrow();
        assertEquals("/filmes/1", location);
        assertEquals(1, json.readTree(created.body()).get("id").asLong());

        var found = get(location);
        assertEquals("Matrix", found.get("titulo").asText());
        assertEquals(136, found.get("duracaoMinutos").asInt());

        var updated = send("PUT", location, filme("Matrix Reloaded", 138));
        assertEquals(200, updated.statusCode());
        assertEquals(1, json.readTree(updated.body()).get("id").asLong(), "ID deve ser preservado");
        assertEquals(138, get(location).get("duracaoMinutos").asInt());

        var removed = send("DELETE", location, null);
        assertEquals(204, removed.statusCode());
        assertEquals("", removed.body());
        assertEquals(404, send("GET", location, null).statusCode());
        assertEquals(404, send("DELETE", location, null).statusCode());
        assertEquals(404, send("PUT", location, filme("Matrix", 100)).statusCode());
    }

    @Test void filtersByPartOfTitleIgnoringCase() throws Exception {
        send("POST", "/filmes", filme("O Senhor dos Anéis", 178));
        send("POST", "/filmes", filme("Matrix", 136));
        send("POST", "/filmes", filme("Matrix Revolutions", 129));
        assertEquals(3, get("/filmes").size());
        assertEquals(2, get("/filmes?titulo=MATRIX").size());
        assertEquals(2, get("/filmes?titulo=trix").size());
        assertEquals(1, get("/filmes?titulo=senhor").size());
        assertEquals(1, get("/filmes?titulo=ANÉIS").size());
        assertEquals(0, get("/filmes?titulo=titanic").size());
    }

    @Test void idsAreGeneratedAutomaticallyAndNeverReused() throws Exception {
        var first = json.readTree(send("POST", "/filmes", filme("A", 90)).body()).get("id").asLong();
        var second = json.readTree(send("POST", "/filmes", filme("B", 90)).body()).get("id").asLong();
        assertEquals(first + 1, second);
        send("DELETE", "/filmes/" + second, null);
        var third = json.readTree(send("POST", "/filmes", filme("C", 90)).body()).get("id").asLong();
        assertTrue(third > second);
    }

    @Test void rejectsInvalidInputsWithStructuredErrors() throws Exception {
        var invalid = send("POST", "/filmes", filme(" ", 0));
        assertEquals(400, invalid.statusCode());
        var error = json.readTree(invalid.body());
        assertEquals(2, error.get("details").size());
        assertEquals(invalid.headers().firstValue("X-Request-Id").orElseThrow(), error.get("requestId").asText());
        assertEquals(400, send("POST", "/filmes", filme("", 100)).statusCode());
        assertEquals(400, send("POST", "/filmes", filme(null, 100)).statusCode());
        assertEquals(400, send("POST", "/filmes", filme("Matrix", -5)).statusCode());
        assertEquals(400, send("POST", "/filmes", filme("Matrix", 0)).statusCode());
        for (String body : List.of("{broken", "null", "", "{}", "{\"titulo\":\"Matrix\",\"duracaoMinutos\":1.5}",
                "{\"titulo\":\"Matrix\",\"duracaoMinutos\":\"100\"}", "{} {}",
                "{\"titulo\":\"Matrix\",\"duracaoMinutos\":100,\"id\":7}")) {
            assertEquals(400, send("POST", "/filmes", body).statusCode(), body);
        }
        assertEquals(400, send("GET", "/filmes/abc", null).statusCode());
        assertEquals(415, client.send(request("POST", "/filmes", "{}", "text/plain"), HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(0, get("/filmes").size(), "Nenhuma entrada inválida pode ter sido gravada");
    }

    @Test void businessRuleLimitsDurationTo600Minutes() throws Exception {
        assertEquals(201, send("POST", "/filmes", filme("Maratona", 600)).statusCode());
        var rejected = send("POST", "/filmes", filme("Maratona longa", 601));
        assertEquals(422, rejected.statusCode());
        assertEquals("Um filme pode ter no máximo 600 minutos", json.readTree(rejected.body()).get("message").asText());
        assertEquals(1, get("/filmes").size());
    }

    @Test void rejectedUpdateDoesNotChangeStoredData() throws Exception {
        send("POST", "/filmes", filme("Matrix", 136));
        assertEquals(400, send("PUT", "/filmes/1", filme(" ", 100)).statusCode());
        assertEquals(400, send("PUT", "/filmes/1", filme("Novo", 0)).statusCode());
        assertEquals(422, send("PUT", "/filmes/1", filme("Novo", 601)).statusCode());
        var stored = get("/filmes/1");
        assertEquals("Matrix", stored.get("titulo").asText());
        assertEquals(136, stored.get("duracaoMinutos").asInt());
        assertEquals(1, get("/filmes").size());
    }

    @Test void updateAppliesSameValidationsAsCreate() throws Exception {
        send("POST", "/filmes", filme("Matrix", 136));
        assertEquals(400, send("PUT", "/filmes/1", "{broken").statusCode());
        assertEquals(400, send("PUT", "/filmes/1", "{}").statusCode());
        assertEquals(200, send("PUT", "/filmes/1", filme("  Neo  ", 600)).statusCode());
        assertEquals("Neo", get("/filmes/1").get("titulo").asText());
    }

    @Test void distinguishesRouteErrorsAndSendsAllowHeader() throws Exception {
        assertEquals(404, send("GET", "/inexistente", null).statusCode());
        assertEquals(404, send("GET", "/filmes/999", null).statusCode());
        var response = send("PATCH", "/filmes/1", "{}");
        assertEquals(405, response.statusCode());
        assertEquals("DELETE, GET, PUT", response.headers().firstValue("Allow").orElseThrow());
    }

    @Test void concurrentRequestsProduceUniqueIds() throws Exception {
        var futures = new ArrayList<CompletableFuture<HttpResponse<String>>>();
        for (int i = 0; i < 30; i++) futures.add(client.sendAsync(
            request("POST", "/filmes", filme("Filme", 90), "application/json"), HttpResponse.BodyHandlers.ofString()));
        var ids = new HashSet<Long>();
        for (var future : futures) {
            var response = future.join();
            assertEquals(201, response.statusCode());
            ids.add(json.readTree(response.body()).get("id").asLong());
        }
        assertEquals(30, ids.size());
        assertEquals(30, get("/filmes").size());
    }
}
