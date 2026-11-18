package br.com.fiap.agrocompartilha.resource;

import br.com.fiap.agrocompartilha.dao.ProdutorDAO;
import br.com.fiap.agrocompartilha.dto.LoginRequest;
import br.com.fiap.agrocompartilha.dto.LoginResponse;
import br.com.fiap.agrocompartilha.dto.ProdutorDTO;
import br.com.fiap.agrocompartilha.service.AutenticacaoService;
import br.com.fiap.agrocompartilha.service.ProdutorService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;

@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AutenticacaoResource {

    private final ProdutorDAO produtorDAO = new ProdutorDAO();
    private final AutenticacaoService autenticacao = new AutenticacaoService(produtorDAO);
    private final ProdutorService produtorService = new ProdutorService(produtorDAO);

    @POST
    @Path("login")
    public LoginResponse entrar(LoginRequest request) throws SQLException {
        Conversor.exigirCorpo(request);
        String token = autenticacao.entrar(request.documento, request.senha);
        Long produtorId = autenticacao.produtorLogado("Bearer " + token);
        return new LoginResponse(token, Conversor.meuPerfil(produtorService.buscarPorId(produtorId)));
    }

    @POST
    @Path("logout")
    public Response sair(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) {
        autenticacao.sair(authorization);
        return Response.noContent().build();
    }

    @GET
    @Path("eu")
    public ProdutorDTO eu(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        return Conversor.meuPerfil(produtorService.buscarPorId(produtorId));
    }
}
