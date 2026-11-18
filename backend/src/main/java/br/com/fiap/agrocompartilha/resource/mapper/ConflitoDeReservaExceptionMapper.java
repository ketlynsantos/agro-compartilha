package br.com.fiap.agrocompartilha.resource.mapper;

import br.com.fiap.agrocompartilha.exception.ConflitoDeReservaException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConflitoDeReservaExceptionMapper implements ExceptionMapper<ConflitoDeReservaException> {
    @Override
    public Response toResponse(ConflitoDeReservaException e) {
        return Erro.resposta(Response.Status.CONFLICT, e.getMessage());
    }
}
