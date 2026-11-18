package br.com.fiap.agrocompartilha.resource;

import br.com.fiap.agrocompartilha.dao.EquipamentoDAO;
import br.com.fiap.agrocompartilha.dao.ProdutorDAO;
import br.com.fiap.agrocompartilha.dao.ReservaDAO;
import br.com.fiap.agrocompartilha.dto.EquipamentoDTO;
import br.com.fiap.agrocompartilha.dto.NovoEquipamentoRequest;
import br.com.fiap.agrocompartilha.dto.PeriodoDTO;
import br.com.fiap.agrocompartilha.exception.ValidacaoException;
import br.com.fiap.agrocompartilha.model.Equipamento;
import br.com.fiap.agrocompartilha.model.ImplementoAgricola;
import br.com.fiap.agrocompartilha.model.MaquinaMotorizada;
import br.com.fiap.agrocompartilha.model.TipoCombustivel;
import br.com.fiap.agrocompartilha.model.TipoEquipamento;
import br.com.fiap.agrocompartilha.service.AutenticacaoService;
import br.com.fiap.agrocompartilha.service.EquipamentoService;
import br.com.fiap.agrocompartilha.service.ReservaService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.List;

@Path("equipamentos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EquipamentoResource {

    private final ProdutorDAO produtorDAO = new ProdutorDAO();
    private final EquipamentoService service = new EquipamentoService(new EquipamentoDAO(), produtorDAO);
    private final AutenticacaoService autenticacao = new AutenticacaoService(produtorDAO);
    private final ReservaService reservaService =
        new ReservaService(new ReservaDAO(), new EquipamentoDAO(), produtorDAO);

    @GET
    public List<EquipamentoDTO> catalogo(@QueryParam("tipo") String tipo) throws SQLException {
        TipoEquipamento filtroTipo = (tipo == null || tipo.isBlank())
            ? null : Conversor.enumeracao(TipoEquipamento.class, tipo, "tipo");
        return service.listarCatalogo().stream()
            .filter(e -> filtroTipo == null || e.getTipo() == filtroTipo)
            .map(Conversor::equipamento)
            .toList();
    }

    @GET
    @Path("meus")
    public List<EquipamentoDTO> meus(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization)
            throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        return service.listarDoDono(produtorId).stream().map(Conversor::equipamento).toList();
    }

    @GET
    @Path("{id}")
    public EquipamentoDTO buscar(@PathParam("id") Long id) throws SQLException {
        return Conversor.equipamento(service.buscarPorId(id));
    }

    @GET
    @Path("{id}/ocupacao")
    public List<PeriodoDTO> ocupacao(@PathParam("id") Long id) throws SQLException {
        service.buscarPorId(id);
        return reservaService.listarPeriodosOcupados(id).stream()
            .map(r -> new PeriodoDTO(r.getDataInicio(), r.getDataFim()))
            .toList();
    }

    @POST
    public Response cadastrar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                              NovoEquipamentoRequest request) throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        Conversor.exigirCorpo(request);
        Equipamento criado = service.cadastrar(produtorId, paraModel(request));
        return Response.status(Response.Status.CREATED).entity(Conversor.equipamento(criado)).build();
    }

    @PUT
    @Path("{id}")
    public EquipamentoDTO atualizar(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                                    @PathParam("id") Long id, NovoEquipamentoRequest request) throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        Conversor.exigirCorpo(request);
        Equipamento dados = paraModel(request);
        dados.setId(id);
        return Conversor.equipamento(service.atualizar(produtorId, dados));
    }

    @DELETE
    @Path("{id}")
    public Response remover(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
                            @PathParam("id") Long id) throws SQLException {
        Long produtorId = autenticacao.produtorLogado(authorization);
        boolean apagado = service.remover(produtorId, id);
        return Response.ok(java.util.Map.of("apagado", apagado)).build();
    }

    private Equipamento paraModel(NovoEquipamentoRequest request) {
        TipoEquipamento tipo = Conversor.enumeracao(TipoEquipamento.class, request.tipo, "tipo");
        Equipamento equipamento;
        if (tipo == TipoEquipamento.MOTORIZADA) {
            if (request.potenciaCv == null) {
                throw new ValidacaoException("Informe a potência da máquina (cv)");
            }
            equipamento = new MaquinaMotorizada(null, Conversor.aparar(request.nome),
                Conversor.aparar(request.descricao), request.valorDiaria, request.potenciaCv,
                Conversor.enumeracao(TipoCombustivel.class, request.tipoCombustivel, "combustível"));
        } else {
            equipamento = new ImplementoAgricola(null, Conversor.aparar(request.nome),
                Conversor.aparar(request.descricao), request.valorDiaria,
                Conversor.aparar(request.tipoImplemento), Conversor.aparar(request.compatibilidade));
        }
        equipamento.setDisponivel(request.disponivel == null || request.disponivel);
        return equipamento;
    }
}
