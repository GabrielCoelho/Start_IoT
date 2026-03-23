package br.edu.fatec.startiot.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String entidade, Long id) {
        return new NotFoundException("%s com id %d não encontrado(a)".formatted(entidade, id));
    }
}
