package br.com.minispring.filmes.model;

/**
 * Entidade principal do domínio: um filme disponível no catálogo da plataforma de streaming.
 * Record imutável; atualizar significa criar outro Filme com o mesmo ID.
 */
public record Filme(long id, String titulo, int duracaoMinutos) {}
