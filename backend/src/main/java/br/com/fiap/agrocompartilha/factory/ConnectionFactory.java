package br.com.fiap.agrocompartilha.factory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abre conexões JDBC com o Oracle.
 * As credenciais vêm das variáveis de ambiente ORACLE_JDBC_URL, ORACLE_USER e ORACLE_PASSWORD
 * ou, se não estiverem definidas, do arquivo .env na pasta onde o programa é executado.
 * Assim nenhuma senha fica escrita no código.
 */
public class ConnectionFactory {

    private static final Map<String, String> ARQUIVO_ENV = carregarArquivoEnv(Path.of(".env"));

    public static Connection getConnection() throws SQLException {
        String url = getEnvOrThrow("ORACLE_JDBC_URL");
        String user = getEnvOrThrow("ORACLE_USER");
        String password = getEnvOrThrow("ORACLE_PASSWORD");
        return DriverManager.getConnection(url, user, password);
    }

    static String getEnvOrThrow(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = ARQUIVO_ENV.get(name);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Variável de ambiente " + name
                + " não configurada (defina no sistema ou no arquivo .env)");
        }
        return value;
    }

    static Map<String, String> carregarArquivoEnv(Path arquivo) {
        Map<String, String> valores = new HashMap<>();
        if (!Files.exists(arquivo)) {
            return valores;
        }
        try {
            List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
            for (String linha : linhas) {
                String texto = linha.trim();
                if (texto.isEmpty() || texto.startsWith("#") || !texto.contains("=")) {
                    continue;
                }
                int separador = texto.indexOf('=');
                valores.put(texto.substring(0, separador).trim(), texto.substring(separador + 1).trim());
            }
        } catch (IOException e) {
            System.out.println("Aviso: não foi possível ler o arquivo .env: " + e.getMessage());
        }
        return valores;
    }
}
