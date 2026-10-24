package br.com.minispring.filmes.model;

import br.com.minispring.framework.validation.NotBlank;
import br.com.minispring.framework.validation.Positive;

/**
 * DTO de entrada, usado no cadastro (POST) e na atualização (PUT).
 * Não possui ID: o cliente nunca escolhe o identificador.
 * Como as duas operações usam o mesmo DTO, as validações estruturais são idênticas.
 */
public record FilmeInput(
        @NotBlank String titulo,
        @Positive int duracaoMinutos) {}
