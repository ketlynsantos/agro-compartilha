package br.com.fiap.agrocompartilha.service;

import br.com.fiap.agrocompartilha.dao.AvaliacaoDAO;
import br.com.fiap.agrocompartilha.dao.ReservaDAO;
import br.com.fiap.agrocompartilha.exception.AcessoNegadoException;
import br.com.fiap.agrocompartilha.exception.EntidadeNaoEncontradaException;
import br.com.fiap.agrocompartilha.exception.ValidacaoException;
import br.com.fiap.agrocompartilha.model.Avaliacao;
import br.com.fiap.agrocompartilha.model.Reserva;
import br.com.fiap.agrocompartilha.model.StatusReserva;

import java.sql.SQLException;
import java.util.List;

public class AvaliacaoService {

    private final AvaliacaoDAO avaliacaoDAO;
    private final ReservaDAO reservaDAO;

    public AvaliacaoService(AvaliacaoDAO avaliacaoDAO, ReservaDAO reservaDAO) {
        this.avaliacaoDAO = avaliacaoDAO;
        this.reservaDAO = reservaDAO;
    }

    public Avaliacao avaliar(Long produtorLogadoId, Long reservaId, int nota, String comentario) throws SQLException {
        if (reservaId == null) {
            throw new ValidacaoException("Informe a reserva avaliada");
        }
        Reserva reserva = reservaDAO.buscarPorId(reservaId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Reserva " + reservaId + " não encontrada"));
        if (!reserva.envolve(produtorLogadoId)) {
            throw new AcessoNegadoException("Só quem participou da reserva pode avaliá-la");
        }
        if (reserva.getStatus() != StatusReserva.CONCLUIDA) {
            throw new ValidacaoException("Só é possível avaliar depois que a reserva for concluída");
        }
        if (avaliacaoDAO.existeAvaliacao(reservaId, produtorLogadoId)) {
            throw new ValidacaoException("Você já avaliou esta reserva");
        }
        if (nota <= 2 && (comentario == null || comentario.trim().length() < 5)) {
            throw new ValidacaoException("Para notas 1 ou 2, conte o que aconteceu no comentário");
        }
        Long avaliadoId = reserva.outraParte(produtorLogadoId).getId();
        String texto = comentario == null ? null : comentario.trim();
        return avaliacaoDAO.inserir(new Avaliacao(reservaId, produtorLogadoId, avaliadoId, nota, texto));
    }

    public List<Avaliacao> listarRecebidas(Long produtorId) throws SQLException {
        return avaliacaoDAO.listarRecebidas(produtorId);
    }
}
