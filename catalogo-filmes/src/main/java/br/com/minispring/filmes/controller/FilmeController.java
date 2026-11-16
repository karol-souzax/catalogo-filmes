package br.com.minispring.filmes.controller;

import br.com.minispring.filmes.model.Filme;
import br.com.minispring.filmes.model.FilmeInput;
import br.com.minispring.filmes.service.FilmeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/filmes")
public class FilmeController {
    private final FilmeService service;

    public FilmeController(FilmeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Filme> list(@RequestParam(defaultValue = "") String titulo) {
        return service.list(titulo);
    }

    @GetMapping("/{id}")
    public Filme get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<Filme> create(@Valid @RequestBody FilmeInput input) {
        Filme filme = service.create(input);
        return ResponseEntity.created(URI.create("/filmes/" + filme.id())).body(filme);
    }

    @PutMapping("/{id}")
    public Filme update(@PathVariable long id, @Valid @RequestBody FilmeInput input) {
        return service.update(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
