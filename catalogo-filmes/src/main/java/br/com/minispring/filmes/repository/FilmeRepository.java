package br.com.minispring.filmes.repository;

import br.com.minispring.filmes.model.Filme;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de armazenamento. O Service depende desta interface;
 * o container de injeção encontra a implementação.
 */
public interface FilmeRepository {
    Filme create(String titulo, int duracaoMinutos);

    List<Filme> findAll();

    Optional<Filme> findById(long id);

    /** Substitui titulo e duração preservando o ID. Vazio se o filme não existir. */
    Optional<Filme> update(long id, String titulo, int duracaoMinutos);

    boolean delete(long id);
}
