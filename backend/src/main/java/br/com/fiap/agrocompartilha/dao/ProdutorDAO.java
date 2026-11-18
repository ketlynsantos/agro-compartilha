package br.com.fiap.agrocompartilha.dao;

import br.com.fiap.agrocompartilha.factory.ConnectionFactory;
import br.com.fiap.agrocompartilha.model.Produtor;
import br.com.fiap.agrocompartilha.model.Reputacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutorDAO {

    private static final String REPUTACAO =
        "(SELECT avaliado_id, AVG(nota) AS media, COUNT(*) AS total FROM avaliacao GROUP BY avaliado_id)";

    private static final String SELECT =
        "SELECT " + colunas("p") + ", p.senha_hash FROM produtor p" + juntarReputacao("p");

    public Produtor inserir(Produtor produtor) throws SQLException {
        String sql = "INSERT INTO produtor (nome, documento, telefone, municipio, senha_hash) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            stmt.setString(1, produtor.getNome());
            stmt.setString(2, produtor.getDocumento());
            stmt.setString(3, produtor.getTelefone());
            stmt.setString(4, produtor.getMunicipio());
            stmt.setString(5, produtor.getSenhaHash());
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    produtor.setId(chaves.getLong(1));
                }
            }
        }
        return produtor;
    }

    public Optional<Produtor> buscarPorId(Long id) throws SQLException {
        return buscarUm(SELECT + " WHERE p.id = ?", id.toString(), true);
    }

    public Optional<Produtor> buscarPorDocumento(String documento) throws SQLException {
        return buscarUm(SELECT + " WHERE p.documento = ?", documento, false);
    }

    private Optional<Produtor> buscarUm(String sql, String parametro, boolean numerico) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (numerico) {
                stmt.setLong(1, Long.parseLong(parametro));
            } else {
                stmt.setString(1, parametro);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Produtor produtor = mapear(rs, "p");
                produtor.setSenhaHash(rs.getString("senha_hash"));
                return Optional.of(produtor);
            }
        }
    }

    public List<Produtor> listarTodos() throws SQLException {
        List<Produtor> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT + " ORDER BY p.nome");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs, "p"));
            }
        }
        return lista;
    }

    public void atualizar(Produtor produtor) throws SQLException {
        String sql = "UPDATE produtor SET nome = ?, documento = ?, telefone = ?, municipio = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produtor.getNome());
            stmt.setString(2, produtor.getDocumento());
            stmt.setString(3, produtor.getTelefone());
            stmt.setString(4, produtor.getMunicipio());
            stmt.setLong(5, produtor.getId());
            stmt.executeUpdate();
        }
    }

    public void atualizarSenha(Long id, String senhaHash) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE produtor SET senha_hash = ? WHERE id = ?")) {
            stmt.setString(1, senhaHash);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    public void excluir(Long id) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM produtor WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    static String colunas(String apelido) {
        String a = apelido;
        return a + ".id AS " + a + "_id, " + a + ".nome AS " + a + "_nome, "
            + a + ".documento AS " + a + "_documento, " + a + ".telefone AS " + a + "_telefone, "
            + a + ".municipio AS " + a + "_municipio, "
            + "rep_" + a + ".media AS " + a + "_media, rep_" + a + ".total AS " + a + "_total";
    }

    static String juntarReputacao(String apelido) {
        return " LEFT JOIN " + REPUTACAO + " rep_" + apelido + " ON rep_" + apelido + ".avaliado_id = " + apelido + ".id";
    }

    static Produtor mapear(ResultSet rs, String apelido) throws SQLException {
        String p = apelido + "_";
        Produtor produtor = new Produtor(rs.getString(p + "nome"), rs.getString(p + "documento"),
            rs.getString(p + "telefone"), rs.getString(p + "municipio"));
        produtor.setId(rs.getLong(p + "id"));
        produtor.setReputacao(new Reputacao(rs.getDouble(p + "media"), rs.getInt(p + "total")));
        return produtor;
    }
}
