package br.com.fiap.agrocompartilha.resource.mapper;

import br.com.fiap.agrocompartilha.exception.NaoAutenticadoException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NaoAutenticadoExceptionMapper implements ExceptionMapper<NaoAutenticadoException> {
    @Override
    public Response toResponse(NaoAutenticadoException e) {
        return Erro.resposta(Response.Status.UNAUTHORIZED, e.getMessage());
    }
}
