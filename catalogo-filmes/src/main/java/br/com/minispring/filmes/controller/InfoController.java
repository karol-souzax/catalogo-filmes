package br.com.minispring.filmes.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InfoController {
    private final String appName;

    public InfoController(@Value("${app.name:Catálogo de Filmes}") String appName) {
        this.appName = appName;
    }

    @GetMapping("/")
    public Map<String, String> info() {
        return Map.of("application", appName, "framework", "Spring Boot", "server", "Embedded Tomcat");
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
