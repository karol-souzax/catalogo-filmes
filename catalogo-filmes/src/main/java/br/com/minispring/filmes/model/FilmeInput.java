package br.com.minispring.filmes.model;

import br.com.minispring.framework.validation.NotBlank;
import br.com.minispring.framework.validation.NotNull;
import br.com.minispring.framework.validation.Positive;


/**
 * DTO de entrada, usado no cadastro (POST) e na atualização (PUT)
 * o cliente nunca escolhe o identificador(id)
 * as anotacoes vem do validador do framework por reflection, antes do controller
 */
public record FilmeInput(
        @NotBlank String titulo,
        @NotNull @Positive Integer duracaoMinutos) {}
