package br.com.fiap.agrocompartilha.resource.mapper;

import br.com.fiap.agrocompartilha.exception.ValidacaoException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidacaoExceptionMapper implements ExceptionMapper<ValidacaoException> {
    @Override
    public Response toResponse(ValidacaoException e) {
        return Erro.resposta(Response.Status.BAD_REQUEST, e.getMessage());
    }
}
