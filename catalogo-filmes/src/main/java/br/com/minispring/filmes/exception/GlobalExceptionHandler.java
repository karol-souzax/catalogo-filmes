package br.com.minispring.filmes.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converte exceções em respostas JSON com o mesmo formato do projeto original:
 * { timestamp, status, message, path, details, requestId }.
 *
 * 400 = forma da entrada inválida; 404 = filme/rota inexistente; 405 = método não suportado;
 * 415 = Content-Type não JSON; 422 = regra de negócio; 500 = falha inesperada.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 e 422 lançados pelo FilmeService.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(ResponseStatusException e, HttpServletRequest request) {
        return error(e.getStatusCode(), e.getReason(), List.of(), request);
    }

    // @Valid falhou: um detalhe por campo, ex.: "titulo não pode estar em branco".
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .sorted()
                .toList();
        return error(HttpStatus.BAD_REQUEST, "Falha de validação", details, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> methodValidation(HandlerMethodValidationException e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Falha de validação", List.of(), request);
    }

    // JSON malformado, corpo vazio/null, tipo incorreto, campo desconhecido (ex.: "id").
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Corpo JSON inválido", List.of(), request);
    }

    // Ex.: GET /filmes/abc (id não numérico).
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> typeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + e.getName(), List.of(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> noRoute(NoResourceFoundException e, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Rota não encontrada", List.of(), request);
    }

    // 405 com o cabeçalho Allow listando os métodos aceitos, ex.: "DELETE, GET, PUT".
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> methodNotAllowed(HttpRequestMethodNotSupportedException e,
                                                                HttpServletRequest request) {
        Set<HttpMethod> supported = e.getSupportedHttpMethods();
        String allow = supported == null ? "" : supported.stream()
                .map(HttpMethod::name).sorted().collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.ALLOW, allow)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido", List.of(), request));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> mediaType(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type deve ser application/json", List.of(), request);
    }

    // Falha inesperada: detalhes técnicos só no log, nunca na resposta.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unexpected(Exception e, HttpServletRequest request) {
        log.error("Erro inesperado requestId={}", RequestIdFilter.of(request), e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", List.of(), request);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatusCode status, String message, List<String> details,
                                                      HttpServletRequest request) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body(status, message, details, request));
    }

    private Map<String, Object> body(HttpStatusCode status, String message, List<String> details,
                                     HttpServletRequest request) {
        var body = new LinkedHashMap<String, Object>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("message", message == null ? "" : message);
        body.put("path", request.getRequestURI());
        body.put("details", details);
        body.put("requestId", RequestIdFilter.of(request));
        return body;
    }
}
