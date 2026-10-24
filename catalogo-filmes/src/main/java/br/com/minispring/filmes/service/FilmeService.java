package br.com.minispring.filmes.service;

import br.com.minispring.filmes.model.Filme;
import br.com.minispring.filmes.model.FilmeInput;
import br.com.minispring.filmes.repository.FilmeRepository;
import br.com.minispring.framework.annotation.Service;
import br.com.minispring.framework.web.HttpException;

import java.util.List;
import java.util.Locale;

@Service
public final class FilmeService {
    /** Regra de negócio: duração máxima de um filme do catálogo. */
    public static final int DURACAO_MAXIMA_MINUTOS = 600;

    private final FilmeRepository repository;

    // Injeção por construtor, dependendo da interface (não da implementação em memória).
    public FilmeService(FilmeRepository repository) {
        this.repository = repository;
    }

    /** Lista todos os filmes cujo título contém o texto informado, ignorando maiúsculas/minúsculas. */
    public List<Filme> list(String titulo) {
        // Locale.ROOT evita que o resultado dependa do idioma configurado na máquina.
        String filtro = titulo.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(f -> f.titulo().toLowerCase(Locale.ROOT).contains(filtro))
                .toList();
    }

    public Filme get(long id) {
        return repository.findById(id).orElseThrow(this::notFound);
    }

    public Filme create(FilmeInput input) {
        // A regra é verificada ANTES de qualquer escrita: uma operação rejeitada não altera os dados.
        validateBusinessRule(input);
        return repository.create(input.titulo().trim(), input.duracaoMinutos());
    }

    public Filme update(long id, FilmeInput input) {
        // Mesma regra do cadastro; só grava depois de aprovada.
        validateBusinessRule(input);
        return repository.update(id, input.titulo().trim(), input.duracaoMinutos())
                .orElseThrow(this::notFound);
    }

    public void delete(long id) {
        if (!repository.delete(id)) throw notFound();
    }

    // @NotBlank e @Positive validam a forma da entrada (400); este limite é regra de domínio (422).
    private void validateBusinessRule(FilmeInput input) {
        if (input.duracaoMinutos() > DURACAO_MAXIMA_MINUTOS) {
            throw new HttpException(422, "Um filme pode ter no máximo " + DURACAO_MAXIMA_MINUTOS + " minutos");
        }
    }

    private HttpException notFound() {
        return new HttpException(404, "Filme não encontrado");
    }
}
