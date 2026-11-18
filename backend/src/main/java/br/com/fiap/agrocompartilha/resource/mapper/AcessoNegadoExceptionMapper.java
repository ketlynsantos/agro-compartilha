package br.com.fiap.agrocompartilha.resource.mapper;

import br.com.fiap.agrocompartilha.exception.AcessoNegadoException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AcessoNegadoExceptionMapper implements ExceptionMapper<AcessoNegadoException> {
    @Override
    public Response toResponse(AcessoNegadoException e) {
        return Erro.resposta(Response.Status.FORBIDDEN, e.getMessage());
    }
}
