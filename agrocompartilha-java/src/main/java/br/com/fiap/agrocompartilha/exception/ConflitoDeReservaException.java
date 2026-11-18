package br.com.fiap.agrocompartilha.exception;

public class ConflitoDeReservaException extends RuntimeException {
    public ConflitoDeReservaException(String mensagem) {
        super(mensagem);
    }
}
