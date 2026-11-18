package br.com.fiap.agrocompartilha.resource;

import br.com.fiap.agrocompartilha.dao.AvaliacaoDAO;
import br.com.fiap.agrocompartilha.dao.ProdutorDAO;
import br.com.fiap.agrocompartilha.dao.ReservaDAO;
import br.com.fiap.agrocompartilha.dto.AvaliacaoDTO;
import br.com.fiap.agrocompartilha.dto.NovaAvaliacaoRequest;
import br.com.fiap.agrocompartilha.exception.ValidacaoException;
import br.com.fiap.agrocompartilha.model.Avaliacao;
import br.com.fiap.agrocompartilha.service.AutenticacaoService;
import br.com.fiap.agrocompartilha.service.AvaliacaoService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;

@Path("avaliacoes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AvaliacaoResource {

    private final AvaliacaoService service = new AvaliacaoService(new AvaliacaoDAO(), new ReservaDAO());
    private final AutenticacaoService autenticacao = new AutenticacaoService(new ProdutorDAO());

    @POST
    public Response avaliar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                            NovaAvaliacaoRequest request) throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        Conversor.exigirCorpo(request);
        if (request.nota == null) {
            throw new ValidacaoException("Escolha uma nota de 1 a 5");
        }
        Avaliacao avaliacao = service.avaliar(produtorId, request.reservaId, request.nota, request.comentario);
        return Response.status(Response.Status.CREATED).entity(Conversor.avaliacao(avaliacao)).build();
    }
}
