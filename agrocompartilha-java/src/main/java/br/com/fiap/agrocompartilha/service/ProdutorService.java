package br.com.fiap.agrocompartilha.service;

import br.com.fiap.agrocompartilha.dao.ProdutorDAO;
import br.com.fiap.agrocompartilha.exception.EntidadeNaoEncontradaException;
import br.com.fiap.agrocompartilha.exception.ValidacaoException;
import br.com.fiap.agrocompartilha.model.Produtor;
import br.com.fiap.agrocompartilha.seguranca.Senha;

import java.sql.SQLException;
import java.util.List;

/**
 * Cadastro e perfil do produtor. Cada produtor só altera e exclui o próprio cadastro:
 * o id vem sempre da sessão, nunca do que o navegador manda.
 */
public class ProdutorService {

    private static final int TAMANHO_MINIMO_SENHA = 6;

    private final ProdutorDAO produtorDAO;

    public ProdutorService(ProdutorDAO produtorDAO) {
        this.produtorDAO = produtorDAO;
    }

    public Produtor cadastrar(Produtor produtor, String senha) throws SQLException {
        validar(produtor);
        validarSenha(senha);
        if (produtorDAO.buscarPorDocumento(produtor.getDocumento()).isPresent()) {
            throw new ValidacaoException("Já existe um cadastro com este CPF/CNPJ");
        }
        produtor.setSenhaHash(Senha.gerarHash(senha));
        return produtorDAO.inserir(produtor);
    }

    public List<Produtor> listarTodos() throws SQLException {
        return produtorDAO.listarTodos();
    }

    public Produtor buscarPorId(Long id) throws SQLException {
        return produtorDAO.buscarPorId(id)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Produtor " + id + " não encontrado"));
    }

    public Produtor atualizarMeuPerfil(Long produtorLogadoId, Produtor dados, String novaSenha) throws SQLException {
        Produtor atual = buscarPorId(produtorLogadoId);
        dados.setId(produtorLogadoId);
        validar(dados);
        if (!atual.getDocumento().equals(dados.getDocumento())
                && produtorDAO.buscarPorDocumento(dados.getDocumento()).isPresent()) {
            throw new ValidacaoException("Já existe um cadastro com este CPF/CNPJ");
        }
        produtorDAO.atualizar(dados);
        if (novaSenha != null && !novaSenha.isEmpty()) {
            validarSenha(novaSenha);
            produtorDAO.atualizarSenha(produtorLogadoId, Senha.gerarHash(novaSenha));
        }
        return buscarPorId(produtorLogadoId);
    }

    public void excluirMinhaConta(Long produtorLogadoId) throws SQLException {
        buscarPorId(produtorLogadoId);
        produtorDAO.excluir(produtorLogadoId);
    }

    static void validar(Produtor produtor) {
        if (!Validacoes.nomeValido(produtor.getNome())) {
            throw new ValidacaoException("Informe um nome válido (pelo menos 3 caracteres, sem símbolos)");
        }
        produtor.setNome(produtor.getNome().trim().replaceAll("\\s+", " "));

        String documento = Validacoes.somenteDigitos(produtor.getDocumento());
        if (documento.length() != 11 && documento.length() != 14) {
            throw new ValidacaoException("O documento deve ser um CPF (11 dígitos) ou CNPJ (14 dígitos)");
        }
        if (!Validacoes.documentoValido(documento)) {
            throw new ValidacaoException(documento.length() == 11 ? "CPF inválido" : "CNPJ inválido");
        }
        produtor.setDocumento(documento);

        String telefone = Validacoes.somenteDigitos(produtor.getTelefone());
        if (!Validacoes.telefoneValido(telefone)) {
            throw new ValidacaoException("Telefone inválido: use DDD + número, ex.: (15) 99876-1234");
        }
        produtor.setTelefone(telefone);

        if (!Validacoes.municipioValido(produtor.getMunicipio())) {
            throw new ValidacaoException("Informe um município válido (só letras, espaço, hífen ou apóstrofo)");
        }
        produtor.setMunicipio(produtor.getMunicipio().trim().replaceAll("\\s+", " "));
    }

    static void validarSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new ValidacaoException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres");
        }
        if (!Validacoes.senhaValida(senha)) {
            throw new ValidacaoException("A senha precisa ter pelo menos uma letra e um número");
        }
    }
}
