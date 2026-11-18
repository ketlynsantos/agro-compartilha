package br.com.fiap.agrocompartilha.resource;

import br.com.fiap.agrocompartilha.dao.AvaliacaoDAO;
import br.com.fiap.agrocompartilha.dao.ProdutorDAO;
import br.com.fiap.agrocompartilha.dao.ReservaDAO;
import br.com.fiap.agrocompartilha.dto.AvaliacaoDTO;
import br.com.fiap.agrocompartilha.dto.LoginResponse;
import br.com.fiap.agrocompartilha.dto.NovoProdutorRequest;
import br.com.fiap.agrocompartilha.dto.ProdutorDTO;
import br.com.fiap.agrocompartilha.model.Produtor;
import br.com.fiap.agrocompartilha.service.AutenticacaoService;
import br.com.fiap.agrocompartilha.service.AvaliacaoService;
import br.com.fiap.agrocompartilha.service.ProdutorService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.List;

@Path("produtores")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProdutorResource {

    private final ProdutorDAO produtorDAO = new ProdutorDAO();
    private final ProdutorService service = new ProdutorService(produtorDAO);
    private final AutenticacaoService autenticacao = new AutenticacaoService(produtorDAO);
    private final AvaliacaoService avaliacaoService = new AvaliacaoService(new AvaliacaoDAO(), new ReservaDAO());

    @POST
    public Response cadastrar(NovoProdutorRequest request) throws SQLException {
        Conversor.exigirCorpo(request);
        Produtor criado = service.cadastrar(paraModel(request), request.senha);
        String token = autenticacao.criarSessao(criado.getId());
        return Response.status(Response.Status.CREATED)
            .entity(new LoginResponse(token, Conversor.meuPerfil(service.buscarPorId(criado.getId()))))
            .build();
    }

    @GET
    public List<ProdutorDTO> listar() throws SQLException {
        return service.listarTodos().stream().map(Conversor::produtorPublico).toList();
    }

    @GET
    @Path("{id}")
    public ProdutorDTO buscar(@PathParam("id") Long id) throws SQLException {
        return Conversor.produtorPublico(service.buscarPorId(id));
    }

    @GET
    @Path("{id}/avaliacoes")
    public List<AvaliacaoDTO> avaliacoesRecebidas(@PathParam("id") Long id) throws SQLException {
        service.buscarPorId(id);
        return avaliacaoService.listarRecebidas(id).stream().map(Conversor::avaliacao).toList();
    }

    @PUT
    @Path("eu")
    public ProdutorDTO atualizarMeuPerfil(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                                          NovoProdutorRequest request) throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        Conversor.exigirCorpo(request);
        Produtor atualizado = service.atualizarMeuPerfil(produtorId, paraModel(request), request.senha);
        return Conversor.meuPerfil(atualizado);
    }

    @DELETE
    @Path("eu")
    public Response excluirMinhaConta(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization)
            throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        service.excluirMinhaConta(produtorId);
        autenticacao.encerrarSessoesDo(produtorId);
        return Response.noContent().build();
    }

    private Produtor paraModel(NovoProdutorRequest request) {
        return new Produtor(Conversor.aparar(request.nome), Conversor.aparar(request.documento),
            Conversor.aparar(request.telefone), Conversor.aparar(request.municipio));
    }
}
