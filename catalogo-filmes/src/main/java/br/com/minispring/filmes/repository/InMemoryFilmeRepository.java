package br.com.minispring.filmes.repository;

import br.com.minispring.filmes.model.Filme;
import br.com.minispring.framework.annotation.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public final class InMemoryFilmeRepository implements FilmeRepository {
    // Bean singleton compartilhado entre requisições concorrentes: estruturas thread-safe.
    private final ConcurrentHashMap<Long, Filme> filmes = new ConcurrentHashMap<>();
    // Gera IDs sequenciais de forma atômica (1, 2, 3...). Reinicia junto com a aplicação.
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Filme create(String titulo, int duracaoMinutos) {
        long id = sequence.incrementAndGet();
        var filme = new Filme(id, titulo, duracaoMinutos);
        filmes.put(id, filme);
        return filme;
    }

    // ConcurrentHashMap não garante ordem; ordenamos por ID para uma listagem previsível.
    @Override
    public List<Filme> findAll() {
        return filmes.values().stream().sorted(Comparator.comparingLong(Filme::id)).toList();
    }

    @Override
    public Optional<Filme> findById(long id) {
        return Optional.ofNullable(filmes.get(id));
    }

    // computeIfPresent é atômico por chave: não recria um filme excluído por outra thread
    // e reutiliza o ID recebido, preservando-o na atualização.
    @Override
    public Optional<Filme> update(long id, String titulo, int duracaoMinutos) {
        return Optional.ofNullable(filmes.computeIfPresent(id, (key, old) -> new Filme(id, titulo, duracaoMinutos)));
    }

    @Override
    public boolean delete(long id) {
        return filmes.remove(id) != null;
    }
}
