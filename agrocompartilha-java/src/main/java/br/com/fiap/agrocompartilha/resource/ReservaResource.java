package br.com.fiap.agrocompartilha.resource;

import br.com.fiap.agrocompartilha.dao.EquipamentoDAO;
import br.com.fiap.agrocompartilha.dao.ProdutorDAO;
import br.com.fiap.agrocompartilha.dao.ReservaDAO;
import br.com.fiap.agrocompartilha.dto.NovaReservaRequest;
import br.com.fiap.agrocompartilha.dto.ReservaDTO;
import br.com.fiap.agrocompartilha.model.Reserva;
import br.com.fiap.agrocompartilha.service.AutenticacaoService;
import br.com.fiap.agrocompartilha.service.ReservaService;
import jakarta.ws.rs.Consumes;
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

@Path("reservas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReservaResource {

    private final ProdutorDAO produtorDAO = new ProdutorDAO();
    private final ReservaService service = new ReservaService(new ReservaDAO(), new EquipamentoDAO(), produtorDAO);
    private final AutenticacaoService autenticacao = new AutenticacaoService(produtorDAO);

    @GET
    @Path("minhas")
    public List<ReservaDTO> minhas(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization) throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        return paraDTO(service.listarMinhasSolicitacoes(produtorId));
    }

    @GET
    @Path("recebidas")
    public List<ReservaDTO> recebidas(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization)
            throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        return paraDTO(service.listarRecebidas(produtorId));
    }

    @GET
    @Path("{id}")
    public ReservaDTO buscar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                             @PathParam("id") Long id) throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        return Conversor.reserva(service.buscar(produtorId, id));
    }

    @POST
    public Response solicitar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                              NovaReservaRequest request) throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        Conversor.exigirCorpo(request);
        Reserva reserva = service.solicitar(produtorId, request.equipamentoId,
            Conversor.data(request.dataInicio, "data de início"), Conversor.data(request.dataFim, "data de fim"));
        return Response.status(Response.Status.CREATED).entity(Conversor.reserva(reserva)).build();
    }

    @PUT
    @Path("{id}/aprovar")
    public ReservaDTO aprovar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                              @PathParam("id") Long id) throws SQLException {
        return Conversor.reserva(service.aprovar(autenticacao.produtorLogado(authorization), id));
    }

    @PUT
    @Path("{id}/rejeitar")
    public ReservaDTO rejeitar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                               @PathParam("id") Long id) throws SQLException {
        return Conversor.reserva(service.rejeitar(autenticacao.produtorLogado(authorization), id));
    }

    @PUT
    @Path("{id}/concluir")
    public ReservaDTO concluir(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                               @PathParam("id") Long id) throws SQLException {
        return Conversor.reserva(service.concluir(autenticacao.produtorLogado(authorization), id));
    }

    @PUT
    @Path("{id}/cancelar")
    public ReservaDTO cancelar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                               @PathParam("id") Long id) throws SQLException {
        return Conversor.reserva(service.cancelar(autenticacao.produtorLogado(authorization), id));
    }

    private List<ReservaDTO> paraDTO(List<Reserva> reservas) {
        return reservas.stream().map(Conversor::reserva).toList();
    }
}
