package br.com.minispring.filmes.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record FilmeInput(
        @NotBlank(message = "O título é obrigatório")
        String titulo,

        @Positive(message = "A duração deve ser maior que zero")
        int duracaoMinutos
) {
}