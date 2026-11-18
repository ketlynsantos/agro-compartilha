package br.com.fiap.agrocompartilha.dao;

import br.com.fiap.agrocompartilha.factory.ConnectionFactory;
import br.com.fiap.agrocompartilha.model.Equipamento;
import br.com.fiap.agrocompartilha.model.Produtor;
import br.com.fiap.agrocompartilha.model.Reserva;
import br.com.fiap.agrocompartilha.model.StatusReserva;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservaDAO {

    private static final String SELECT =
        "SELECT r.id AS r_id, r.data_inicio, r.data_fim, r.status, r.valor_total, "
            + ProdutorDAO.colunas("s") + ", " + EquipamentoDAO.COLUNAS + ", "
            + "(SELECT COUNT(*) FROM avaliacao a WHERE a.reserva_id = r.id "
            + "AND a.avaliador_id = r.produtor_solicitante_id) AS avaliou_solicitante, "
            + "(SELECT COUNT(*) FROM avaliacao a WHERE a.reserva_id = r.id "
            + "AND a.avaliador_id = e.produtor_id) AS avaliou_dono"
            + " FROM reserva r"
            + " JOIN produtor s ON s.id = r.produtor_solicitante_id"
            + ProdutorDAO.juntarReputacao("s")
            + " JOIN equipamento e ON e.id = r.equipamento_id"
            + EquipamentoDAO.JOINS;

    public Reserva inserir(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reserva (produtor_solicitante_id, equipamento_id, data_inicio, data_fim, "
            + "status, valor_total) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            stmt.setLong(1, reserva.getSolicitante().getId());
            stmt.setLong(2, reserva.getEquipamento().getId());
            stmt.setDate(3, Date.valueOf(reserva.getDataInicio()));
            stmt.setDate(4, Date.valueOf(reserva.getDataFim()));
            stmt.setString(5, reserva.getStatus().name());
            stmt.setBigDecimal(6, reserva.getValorTotal());
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    reserva.setId(chaves.getLong(1));
                }
            }
        }
        return reserva;
    }

    public Optional<Reserva> buscarPorId(Long id) throws SQLException {
        List<Reserva> resultado = listar(SELECT + " WHERE r.id = ?", id);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<Reserva> listarAprovadasPorEquipamento(Long equipamentoId) throws SQLException {
        return listar(SELECT + " WHERE r.equipamento_id = ? AND r.status = 'APROVADA' ORDER BY r.data_inicio",
            equipamentoId);
    }

    public List<Reserva> listarPorSolicitante(Long produtorId) throws SQLException {
        return listar(SELECT + " WHERE r.produtor_solicitante_id = ? ORDER BY r.data_inicio DESC", produtorId);
    }

    public List<Reserva> listarPorDono(Long produtorDonoId) throws SQLException {
        return listar(SELECT + " WHERE e.produtor_id = ? ORDER BY r.data_inicio DESC", produtorDonoId);
    }

    public void atualizarStatus(Reserva reserva) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE reserva SET status = ? WHERE id = ?")) {
            stmt.setString(1, reserva.getStatus().name());
            stmt.setLong(2, reserva.getId());
            stmt.executeUpdate();
        }
    }

    private List<Reserva> listar(String sql, Long parametro) throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        Produtor solicitante = ProdutorDAO.mapear(rs, "s");
        Equipamento equipamento = EquipamentoDAO.mapear(rs);
        Reserva reserva = new Reserva(solicitante, equipamento,
            rs.getDate("data_inicio").toLocalDate(), rs.getDate("data_fim").toLocalDate());
        reserva.setId(rs.getLong("r_id"));
        reserva.setStatus(StatusReserva.valueOf(rs.getString("status")));
        reserva.setValorTotal(rs.getBigDecimal("valor_total"));
        reserva.setAvaliadaPeloSolicitante(rs.getInt("avaliou_solicitante") > 0);
        reserva.setAvaliadaPeloDono(rs.getInt("avaliou_dono") > 0);
        return reserva;
    }
}
