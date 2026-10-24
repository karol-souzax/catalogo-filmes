package br.com.minispring.filmes.controller;

import br.com.minispring.filmes.model.Filme;
import br.com.minispring.filmes.model.FilmeInput;
import br.com.minispring.filmes.service.FilmeService;
import br.com.minispring.framework.annotation.*;
import br.com.minispring.framework.web.HttpResult;

import java.util.List;

@RestController("/filmes")
public final class FilmeController {
    private final FilmeService service;

    // DI por construtor: o container fornece o service.
    public FilmeController(FilmeService service) { this.service = service; }

    // GET /filmes?titulo=matrix. Sem o parâmetro, o padrão vazio lista todos os filmes.
    @Route(method = "GET")
    public List<Filme> list(@RequestParam("titulo") String titulo) { return service.list(titulo); }

    @Route(method = "GET", path = "/{id}")
    public Filme get(@PathVariable("id") long id) { return service.get(id); }

    // 201 + cabeçalho Location apontando para o novo recurso.
    @Route(method = "POST")
    public HttpResult create(@RequestBody FilmeInput input) {
        Filme filme = service.create(input);
        return HttpResult.created("/filmes/" + filme.id(), filme);
    }

    // O ID vem da URL e é preservado; titulo e duração vêm do corpo.
    @Route(method = "PUT", path = "/{id}")
    public Filme update(@PathVariable("id") long id, @RequestBody FilmeInput input) {
        return service.update(id, input);
    }

    // 204 sem corpo.
    @Route(method = "DELETE", path = "/{id}")
    public HttpResult delete(@PathVariable("id") long id) {
        service.delete(id);
        return HttpResult.noContent();
    }
}
