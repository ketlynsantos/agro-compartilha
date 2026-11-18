package br.com.fiap.agrocompartilha.resource.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.sql.SQLException;

@Provider
public class SQLExceptionMapper implements ExceptionMapper<SQLException> {

    @Override
    public Response toResponse(SQLException e) {
        System.out.println("Erro de banco [ORA-" + e.getErrorCode() + "]: " + e.getMessage());
        return switch (e.getErrorCode()) {
            case 1 -> Erro.resposta(Response.Status.CONFLICT,
                "Já existe um cadastro com esses dados (ex.: documento repetido)");
            case 2292 -> Erro.resposta(Response.Status.CONFLICT,
                "Não é possível excluir: existem equipamentos, reservas ou avaliações ligados a este registro");
            case 2291 -> Erro.resposta(Response.Status.BAD_REQUEST,
                "Registro relacionado não encontrado no banco");
            case 2290 -> Erro.resposta(Response.Status.BAD_REQUEST,
                "Algum valor informado não é permitido pelas regras do banco");
            case 1400 -> Erro.resposta(Response.Status.BAD_REQUEST, "Há campos obrigatórios não preenchidos");
            case 12899 -> Erro.resposta(Response.Status.BAD_REQUEST, "Algum campo ultrapassa o tamanho permitido");
            case 942 -> Erro.resposta(Response.Status.INTERNAL_SERVER_ERROR,
                "Tabelas não encontradas: rode o script db/schema.sql no Oracle");
            case 1017 -> Erro.resposta(Response.Status.SERVICE_UNAVAILABLE,
                "Usuário ou senha do Oracle inválidos: confira o arquivo .env");
            default -> Erro.resposta(Response.Status.SERVICE_UNAVAILABLE,
                "Não foi possível acessar o banco de dados. Tente novamente em instantes.");
        };
    }
}
