package br.com.fiap.agrocompartilha.service;

import br.com.fiap.agrocompartilha.dao.ProdutorDAO;
import br.com.fiap.agrocompartilha.exception.NaoAutenticadoException;
import br.com.fiap.agrocompartilha.model.Produtor;
import br.com.fiap.agrocompartilha.seguranca.Senha;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Login com documento + senha.
 * Quem acerta a senha recebe um token aleatório, válido por 8 horas,
 * que o front envia no cabeçalho "Authorization: Bearer <token>" das próximas requisições.
 * As sessões ficam em memória: reiniciar o servidor desloga todo mundo.
 */
public class AutenticacaoService {

    private static final int HORAS_DE_SESSAO = 8;
    private static final Map<String, Sessao> SESSOES = new ConcurrentHashMap<>();
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final ProdutorDAO produtorDAO;

    public AutenticacaoService(ProdutorDAO produtorDAO) {
        this.produtorDAO = produtorDAO;
    }

    /** Devolve o token da nova sessão. A mensagem de erro é a mesma para documento ou senha errados. */
    public String entrar(String documento, String senha) throws SQLException {
        String digitos = documento == null ? "" : documento.replaceAll("\\D", "");
        Produtor produtor = produtorDAO.buscarPorDocumento(digitos).orElse(null);
        if (produtor == null || !Senha.confere(senha, produtor.getSenhaHash())) {
            throw new NaoAutenticadoException("CPF/CNPJ ou senha incorretos");
        }
        return criarSessao(produtor.getId());
    }

    public String criarSessao(Long produtorId) {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        SESSOES.put(token, new Sessao(produtorId, LocalDateTime.now().plusHours(HORAS_DE_SESSAO)));
        return token;
    }

    public void sair(String cabecalhoAuthorization) {
        String token = extrairToken(cabecalhoAuthorization);
        if (token != null) {
            SESSOES.remove(token);
        }
    }

    public void encerrarSessoesDo(Long produtorId) {
        SESSOES.values().removeIf(sessao -> sessao.produtorId.equals(produtorId));
    }

    public Long produtorLogado(String cabecalhoAuthorization) {
        String token = extrairToken(cabecalhoAuthorization);
        if (token == null) {
            throw new NaoAutenticadoException("Faça login para continuar");
        }
        Sessao sessao = SESSOES.get(token);
        if (sessao == null || sessao.expiraEm.isBefore(LocalDateTime.now())) {
            SESSOES.remove(token);
            throw new NaoAutenticadoException("Sua sessão expirou ou não é válida. Entre novamente");
        }
        return sessao.produtorId;
    }

    private String extrairToken(String cabecalho) {
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            return null;
        }
        String token = cabecalho.substring("Bearer ".length()).trim();
        return token.isEmpty() ? null : token;
    }

    private static class Sessao {
        private final Long produtorId;
        private final LocalDateTime expiraEm;

        private Sessao(Long produtorId, LocalDateTime expiraEm) {
            this.produtorId = produtorId;
            this.expiraEm = expiraEm;
        }
    }
}
