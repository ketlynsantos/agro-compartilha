package br.com.fiap.agrocompartilha.service;

import br.com.fiap.agrocompartilha.dao.EquipamentoDAO;
import br.com.fiap.agrocompartilha.dao.ProdutorDAO;
import br.com.fiap.agrocompartilha.dao.ReservaDAO;
import br.com.fiap.agrocompartilha.exception.AcessoNegadoException;
import br.com.fiap.agrocompartilha.exception.ConflitoDeReservaException;
import br.com.fiap.agrocompartilha.exception.EntidadeNaoEncontradaException;
import br.com.fiap.agrocompartilha.exception.ValidacaoException;
import br.com.fiap.agrocompartilha.model.Equipamento;
import br.com.fiap.agrocompartilha.model.Produtor;
import br.com.fiap.agrocompartilha.model.Reserva;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservaService {

    private final ReservaDAO reservaDAO;
    private final EquipamentoDAO equipamentoDAO;
    private final ProdutorDAO produtorDAO;

    public ReservaService(ReservaDAO reservaDAO, EquipamentoDAO equipamentoDAO, ProdutorDAO produtorDAO) {
        this.reservaDAO = reservaDAO;
        this.equipamentoDAO = equipamentoDAO;
        this.produtorDAO = produtorDAO;
    }

    public Reserva solicitar(Long produtorLogadoId, Long equipamentoId, LocalDate inicio, LocalDate fim)
            throws SQLException {
        if (equipamentoId == null) {
            throw new ValidacaoException("Informe o equipamento");
        }
        if (inicio != null && inicio.isBefore(LocalDate.now())) {
            throw new ValidacaoException("A data de início não pode estar no passado");
        }
        Produtor solicitante = produtorDAO.buscarPorId(produtorLogadoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Produtor não encontrado"));
        Equipamento equipamento = equipamentoDAO.buscarPorId(equipamentoId)
            .filter(Equipamento::isAtivo)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Equipamento " + equipamentoId + " não encontrado"));
        if (equipamento.pertenceA(produtorLogadoId)) {
            throw new ValidacaoException("Você não pode reservar o seu próprio equipamento");
        }
        if (!equipamento.podeSerReservado()) {
            throw new ValidacaoException("Este equipamento está indisponível no momento");
        }
        Reserva reserva = new Reserva(solicitante, equipamento, inicio, fim);
        // Pedidos pendentes podem se sobrepor entre si, mas um pedido que bate com datas
        // JÁ APROVADAS nunca poderia ser aprovado: é recusado logo, para não criar pedido inútil.
        List<Reserva> aprovadas = reservaDAO.listarAprovadasPorEquipamento(equipamentoId);
        if (aprovadas.stream().anyMatch(reserva::sobrepoe)) {
            throw new ConflitoDeReservaException("Essas datas já estão reservadas para este equipamento. Escolha outro período.");
        }
        reserva.calcularValorTotal();
        return reservaDAO.inserir(reserva);
    }

    public List<Reserva> listarPeriodosOcupados(Long equipamentoId) throws SQLException {
        LocalDate hoje = LocalDate.now();
        return reservaDAO.listarAprovadasPorEquipamento(equipamentoId).stream()
            .filter(r -> !r.getDataFim().isBefore(hoje))
            .toList();
    }

    public List<Reserva> listarMinhasSolicitacoes(Long produtorLogadoId) throws SQLException {
        return reservaDAO.listarPorSolicitante(produtorLogadoId);
    }

    public List<Reserva> listarRecebidas(Long produtorLogadoId) throws SQLException {
        return reservaDAO.listarPorDono(produtorLogadoId);
    }

    public Reserva buscar(Long produtorLogadoId, Long reservaId) throws SQLException {
        Reserva reserva = buscarOuLancar(reservaId);
        if (!reserva.envolve(produtorLogadoId)) {
            throw new AcessoNegadoException("Esta reserva não é sua");
        }
        return reserva;
    }

    public Reserva aprovar(Long produtorLogadoId, Long reservaId) throws SQLException {
        Reserva reserva = buscarComoDono(produtorLogadoId, reservaId);
        List<Reserva> aprovadas = reservaDAO.listarAprovadasPorEquipamento(reserva.getEquipamento().getId());
        if (aprovadas.stream().anyMatch(reserva::sobrepoe)) {
            throw new ConflitoDeReservaException("Já existe uma reserva aprovada nessas datas para este equipamento");
        }
        reserva.aprovar(LocalDate.now());
        reservaDAO.atualizarStatus(reserva);
        return reserva;
    }

    public Reserva rejeitar(Long produtorLogadoId, Long reservaId) throws SQLException {
        Reserva reserva = buscarComoDono(produtorLogadoId, reservaId);
        reserva.rejeitar();
        reservaDAO.atualizarStatus(reserva);
        return reserva;
    }

    public Reserva concluir(Long produtorLogadoId, Long reservaId) throws SQLException {
        Reserva reserva = buscarComoDono(produtorLogadoId, reservaId);
        reserva.concluir(LocalDate.now());
        reservaDAO.atualizarStatus(reserva);
        return reserva;
    }

    public Reserva cancelar(Long produtorLogadoId, Long reservaId) throws SQLException {
        Reserva reserva = buscarOuLancar(reservaId);
        if (!reserva.getSolicitante().getId().equals(produtorLogadoId)) {
            throw new AcessoNegadoException("Só quem fez o pedido pode cancelá-lo");
        }
        reserva.cancelar(LocalDate.now());
        reservaDAO.atualizarStatus(reserva);
        return reserva;
    }

    private Reserva buscarComoDono(Long produtorLogadoId, Long reservaId) throws SQLException {
        Reserva reserva = buscarOuLancar(reservaId);
        if (!reserva.getEquipamento().pertenceA(produtorLogadoId)) {
            throw new AcessoNegadoException("Só o dono do equipamento pode fazer isso");
        }
        return reserva;
    }

    private Reserva buscarOuLancar(Long reservaId) throws SQLException {
        return reservaDAO.buscarPorId(reservaId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Reserva " + reservaId + " não encontrada"));
    }
}
