package br.com.minispring.filmes.exception;

public class FilmeNotFoundException extends RuntimeException {
    public FilmeNotFoundException() {
        super("Filme não encontrado");
    }
}
