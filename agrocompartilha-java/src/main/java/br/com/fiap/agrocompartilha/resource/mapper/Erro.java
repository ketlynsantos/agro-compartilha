package br.com.fiap.agrocompartilha.resource.mapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

final class Erro {

    private Erro() {
    }

    static Response resposta(Response.Status status, String mensagem) {
        return resposta(status.getStatusCode(), mensagem);
    }

    static Response resposta(int status, String mensagem) {
        return Response.status(status)
            .type(MediaType.APPLICATION_JSON)
            .entity(Map.of("erro", mensagem == null ? "Erro inesperado" : mensagem))
            .build();
    }
}
