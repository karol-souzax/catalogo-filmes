package br.com.minispring;

import br.com.minispring.filmes.model.FilmeInput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmeApiIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void healthEndpointReturnsUp() {
        ResponseEntity<String> response = rest.getForEntity("/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    void shouldCreateAndFetchFilm() {
        ResponseEntity<String> created = rest.postForEntity("/filmes",
                new FilmeInput("Matrix", 136), String.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getHeaders().getLocation()).isNotNull();

        ResponseEntity<String> listed = rest.getForEntity("/filmes?titulo=matrix", String.class);
        assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listed.getBody()).contains("Matrix");
    }

    @Test
    void shouldRejectDurationAboveLimit() {
        ResponseEntity<String> response = rest.postForEntity("/filmes",
                new FilmeInput("Filme longo", 601), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
