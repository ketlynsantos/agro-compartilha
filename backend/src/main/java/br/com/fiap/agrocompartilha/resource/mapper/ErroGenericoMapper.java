package br.com.fiap.agrocompartilha.resource.mapper;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ErroGenericoMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        if (e instanceof WebApplicationException web) {
            int status = web.getResponse().getStatus();
            String mensagem = switch (status) {
                case 400 -> "Requisição inválida: confira os dados enviados";
                case 404 -> "Endereço não encontrado na API";
                case 405 -> "Operação não permitida neste endereço";
                case 415 -> "Envie os dados no formato JSON";
                default -> web.getMessage();
            };
            return Erro.resposta(status, mensagem);
        }
        if (e instanceof ProcessingException) {
            return Erro.resposta(Response.Status.BAD_REQUEST, "JSON inválido: confira os tipos dos campos");
        }
        System.out.println("Erro inesperado: " + e);
        e.printStackTrace();
        return Erro.resposta(Response.Status.INTERNAL_SERVER_ERROR, "Erro inesperado no servidor");
    }
}
