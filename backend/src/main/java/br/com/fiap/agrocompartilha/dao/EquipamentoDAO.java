package br.com.fiap.agrocompartilha.dao;

import br.com.fiap.agrocompartilha.factory.ConnectionFactory;
import br.com.fiap.agrocompartilha.model.Equipamento;
import br.com.fiap.agrocompartilha.model.ImplementoAgricola;
import br.com.fiap.agrocompartilha.model.MaquinaMotorizada;
import br.com.fiap.agrocompartilha.model.Produtor;
import br.com.fiap.agrocompartilha.model.TipoCombustivel;
import br.com.fiap.agrocompartilha.model.TipoEquipamento;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EquipamentoDAO {

    static final String COLUNAS =
        "e.id AS e_id, e.nome AS e_nome, e.descricao AS e_descricao, e.tipo AS e_tipo, "
            + "e.valor_diaria AS e_valor_diaria, e.disponivel AS e_disponivel, e.ativo AS e_ativo, "
            + ProdutorDAO.colunas("p") + ", "
            + "m.potencia_cv, m.tipo_combustivel, i.tipo_implemento, i.compatibilidade";

    static final String JOINS =
        " JOIN produtor p ON p.id = e.produtor_id"
            + ProdutorDAO.juntarReputacao("p")
            + " LEFT JOIN maquina_motorizada m ON m.equipamento_id = e.id"
            + " LEFT JOIN implemento_agricola i ON i.equipamento_id = e.id";

    private static final String SELECT = "SELECT " + COLUNAS + " FROM equipamento e" + JOINS;

    public Equipamento inserir(Equipamento equipamento) throws SQLException {
        String sqlBase = "INSERT INTO equipamento (produtor_id, nome, descricao, tipo, valor_diaria, disponivel, ativo) "
            + "VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlBase, new String[]{"ID"})) {
                    stmt.setLong(1, equipamento.getDono().getId());
                    stmt.setString(2, equipamento.getNome());
                    stmt.setString(3, equipamento.getDescricao());
                    stmt.setString(4, equipamento.getTipo().name());
                    definirValor(stmt, 5, equipamento.getValorDiaria());
                    stmt.setInt(6, equipamento.isDisponivel() ? 1 : 0);
                    stmt.executeUpdate();
                    try (ResultSet chaves = stmt.getGeneratedKeys()) {
                        if (chaves.next()) {
                            equipamento.setId(chaves.getLong(1));
                        }
                    }
                }
                inserirSubtipo(conn, equipamento);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return equipamento;
    }

    private void inserirSubtipo(Connection conn, Equipamento equipamento) throws SQLException {
        if (equipamento instanceof MaquinaMotorizada maquina) {
            String sql = "INSERT INTO maquina_motorizada (equipamento_id, potencia_cv, tipo_combustivel) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, maquina.getId());
                stmt.setDouble(2, maquina.getPotenciaCv());
                stmt.setString(3, maquina.getTipoCombustivel().name());
                stmt.executeUpdate();
            }
        } else if (equipamento instanceof ImplementoAgricola implemento) {
            String sql = "INSERT INTO implemento_agricola (equipamento_id, tipo_implemento, compatibilidade) "
                + "VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, implemento.getId());
                stmt.setString(2, implemento.getTipoImplemento());
                stmt.setString(3, implemento.getCompatibilidade());
                stmt.executeUpdate();
            }
        }
    }

    public Optional<Equipamento> buscarPorId(Long id) throws SQLException {
        List<Equipamento> resultado = listar(SELECT + " WHERE e.id = ?", id);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    public List<Equipamento> listarAtivos() throws SQLException {
        return listar(SELECT + " WHERE e.ativo = 1 ORDER BY e.nome", null);
    }

    public List<Equipamento> listarAtivosPorDono(Long produtorId) throws SQLException {
        return listar(SELECT + " WHERE e.ativo = 1 AND e.produtor_id = ? ORDER BY e.nome", produtorId);
    }

    private List<Equipamento> listar(String sql, Long parametro) throws SQLException {
        List<Equipamento> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (parametro != null) {
                stmt.setLong(1, parametro);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public void atualizar(Equipamento equipamento) throws SQLException {
        String sqlBase = "UPDATE equipamento SET nome = ?, descricao = ?, valor_diaria = ?, disponivel = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlBase)) {
                    stmt.setString(1, equipamento.getNome());
                    stmt.setString(2, equipamento.getDescricao());
                    definirValor(stmt, 3, equipamento.getValorDiaria());
                    stmt.setInt(4, equipamento.isDisponivel() ? 1 : 0);
                    stmt.setLong(5, equipamento.getId());
                    stmt.executeUpdate();
                }
                atualizarSubtipo(conn, equipamento);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void atualizarSubtipo(Connection conn, Equipamento equipamento) throws SQLException {
        if (equipamento instanceof MaquinaMotorizada maquina) {
            String sql = "UPDATE maquina_motorizada SET potencia_cv = ?, tipo_combustivel = ? WHERE equipamento_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, maquina.getPotenciaCv());
                stmt.setString(2, maquina.getTipoCombustivel().name());
                stmt.setLong(3, maquina.getId());
                stmt.executeUpdate();
            }
        } else if (equipamento instanceof ImplementoAgricola implemento) {
            String sql = "UPDATE implemento_agricola SET tipo_implemento = ?, compatibilidade = ? WHERE equipamento_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, implemento.getTipoImplemento());
                stmt.setString(2, implemento.getCompatibilidade());
                stmt.setLong(3, implemento.getId());
                stmt.executeUpdate();
            }
        }
    }

    /**
     * Exclusão lógica: o equipamento sai do catálogo, mas a linha continua no banco
     * para não apagar o histórico de reservas e avaliações ligado a ele.
     */
    public void desativar(Long id) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE equipamento SET ativo = 0, disponivel = 0 WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public boolean possuiReservas(Long id) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM reserva WHERE equipamento_id = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Exclusão física (DELETE), usada só para equipamento que nunca teve reserva.
     * Apaga primeiro a linha da subclasse e depois a do equipamento, na mesma transação.
     */
    public void excluir(Long id) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (String sql : new String[]{
                    "DELETE FROM maquina_motorizada WHERE equipamento_id = ?",
                    "DELETE FROM implemento_agricola WHERE equipamento_id = ?",
                    "DELETE FROM equipamento WHERE id = ?"}) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setLong(1, id);
                        stmt.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void definirValor(PreparedStatement stmt, int posicao, BigDecimal valor) throws SQLException {
        if (valor == null) {
            stmt.setNull(posicao, Types.NUMERIC);
        } else {
            stmt.setBigDecimal(posicao, valor);
        }
    }

    static Equipamento mapear(ResultSet rs) throws SQLException {
        Produtor dono = ProdutorDAO.mapear(rs, "p");
        TipoEquipamento tipo = TipoEquipamento.valueOf(rs.getString("e_tipo"));
        Equipamento equipamento;
        if (tipo == TipoEquipamento.MOTORIZADA) {
            equipamento = new MaquinaMotorizada(dono, rs.getString("e_nome"), rs.getString("e_descricao"),
                rs.getBigDecimal("e_valor_diaria"), rs.getDouble("potencia_cv"),
                TipoCombustivel.valueOf(rs.getString("tipo_combustivel")));
        } else {
            equipamento = new ImplementoAgricola(dono, rs.getString("e_nome"), rs.getString("e_descricao"),
                rs.getBigDecimal("e_valor_diaria"), rs.getString("tipo_implemento"), rs.getString("compatibilidade"));
        }
        equipamento.setId(rs.getLong("e_id"));
        equipamento.setDisponivel(rs.getInt("e_disponivel") == 1);
        equipamento.setAtivo(rs.getInt("e_ativo") == 1);
        return equipamento;
    }
}
