package br.com.fiap.agrocompartilha.dao;

import br.com.fiap.agrocompartilha.factory.ConnectionFactory;
import br.com.fiap.agrocompartilha.model.Avaliacao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoDAO {

    public Avaliacao inserir(Avaliacao avaliacao) throws SQLException {
        String sql = "INSERT INTO avaliacao (reserva_id, avaliador_id, avaliado_id, nota, comentario, data_avaliacao) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            stmt.setLong(1, avaliacao.getReservaId());
            stmt.setLong(2, avaliacao.getAvaliadorId());
            stmt.setLong(3, avaliacao.getAvaliadoId());
            stmt.setInt(4, avaliacao.getNota());
            stmt.setString(5, avaliacao.getComentario());
            stmt.setDate(6, Date.valueOf(avaliacao.getData()));
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    avaliacao.setId(chaves.getLong(1));
                }
            }
        }
        return avaliacao;
    }

    public List<Avaliacao> listarRecebidas(Long produtorId) throws SQLException {
        String sql = "SELECT a.id, a.reserva_id, a.avaliador_id, a.avaliado_id, a.nota, a.comentario, a.data_avaliacao, "
            + "av.nome AS avaliador_nome, e.nome AS equipamento_nome "
            + "FROM avaliacao a "
            + "JOIN produtor av ON av.id = a.avaliador_id "
            + "JOIN reserva r ON r.id = a.reserva_id "
            + "JOIN equipamento e ON e.id = r.equipamento_id "
            + "WHERE a.avaliado_id = ? ORDER BY a.data_avaliacao DESC, a.id DESC";
        List<Avaliacao> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, produtorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Avaliacao avaliacao = new Avaliacao(rs.getLong("reserva_id"), rs.getLong("avaliador_id"),
                        rs.getLong("avaliado_id"), rs.getInt("nota"), rs.getString("comentario"),
                        rs.getDate("data_avaliacao").toLocalDate());
                    avaliacao.setId(rs.getLong("id"));
                    avaliacao.setAvaliadorNome(rs.getString("avaliador_nome"));
                    avaliacao.setEquipamentoNome(rs.getString("equipamento_nome"));
                    lista.add(avaliacao);
                }
            }
        }
        return lista;
    }

    public boolean existeAvaliacao(Long reservaId, Long avaliadorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM avaliacao WHERE reserva_id = ? AND avaliador_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, reservaId);
            stmt.setLong(2, avaliadorId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
