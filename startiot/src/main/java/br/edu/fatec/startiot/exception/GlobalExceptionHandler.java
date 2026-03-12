package br.edu.fatec.startiot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP_PROPERTY = "timestamp";

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://startiot.fatec.br/errors/not-found"));
        problem.setTitle("Recurso não encontrado");
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(URI.create("https://startiot.fatec.br/errors/business-rule"));
        problem.setTitle("Violação de regra de negócio");
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("https://startiot.fatec.br/errors/conflict"));
        problem.setTitle("Conflito de dados");
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> campos = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "inválido",
                        (existing, replacement) -> existing
                ));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos possuem valores inválidos"
        );
        problem.setType(URI.create("https://startiot.fatec.br/errors/validation"));
        problem.setTitle("Erro de validação");
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        problem.setProperty("campos", campos);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Por favor, tente novamente."
        );
        problem.setType(URI.create("https://startiot.fatec.br/errors/internal"));
        problem.setTitle("Erro interno do servidor");
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }
}
