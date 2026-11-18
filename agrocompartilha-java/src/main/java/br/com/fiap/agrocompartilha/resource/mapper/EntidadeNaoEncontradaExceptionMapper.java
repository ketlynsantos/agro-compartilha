package br.com.fiap.agrocompartilha.resource.mapper;

import br.com.fiap.agrocompartilha.exception.EntidadeNaoEncontradaException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EntidadeNaoEncontradaExceptionMapper implements ExceptionMapper<EntidadeNaoEncontradaException> {
    @Override
    public Response toResponse(EntidadeNaoEncontradaException e) {
        return Erro.resposta(Response.Status.NOT_FOUND, e.getMessage());
    }
}
