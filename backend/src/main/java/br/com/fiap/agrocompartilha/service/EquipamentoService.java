package br.com.fiap.agrocompartilha.service;

import br.com.fiap.agrocompartilha.dao.EquipamentoDAO;
import br.com.fiap.agrocompartilha.dao.ProdutorDAO;
import br.com.fiap.agrocompartilha.exception.AcessoNegadoException;
import br.com.fiap.agrocompartilha.exception.EntidadeNaoEncontradaException;
import br.com.fiap.agrocompartilha.exception.ValidacaoException;
import br.com.fiap.agrocompartilha.model.Equipamento;
import br.com.fiap.agrocompartilha.model.ImplementoAgricola;
import br.com.fiap.agrocompartilha.model.MaquinaMotorizada;

import java.sql.SQLException;
import java.util.List;

public class EquipamentoService {

    private final EquipamentoDAO equipamentoDAO;
    private final ProdutorDAO produtorDAO;

    public EquipamentoService(EquipamentoDAO equipamentoDAO, ProdutorDAO produtorDAO) {
        this.equipamentoDAO = equipamentoDAO;
        this.produtorDAO = produtorDAO;
    }

    public Equipamento cadastrar(Long produtorLogadoId, Equipamento equipamento) throws SQLException {
        validar(equipamento);
        equipamento.setDono(produtorDAO.buscarPorId(produtorLogadoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Produtor não encontrado")));
        return equipamentoDAO.inserir(equipamento);
    }

    public Equipamento buscarPorId(Long id) throws SQLException {
        return equipamentoDAO.buscarPorId(id)
            .filter(Equipamento::isAtivo)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Equipamento " + id + " não encontrado"));
    }

    public List<Equipamento> listarCatalogo() throws SQLException {
        return equipamentoDAO.listarAtivos();
    }

    public List<Equipamento> listarDoDono(Long produtorId) throws SQLException {
        return equipamentoDAO.listarAtivosPorDono(produtorId);
    }

    public Equipamento atualizar(Long produtorLogadoId, Equipamento dados) throws SQLException {
        Equipamento atual = buscarDoDono(produtorLogadoId, dados.getId());
        if (atual.getTipo() != dados.getTipo()) {
            throw new ValidacaoException("Não é possível trocar o tipo de um equipamento já cadastrado");
        }
        validar(dados);
        dados.setDono(atual.getDono());
        equipamentoDAO.atualizar(dados);
        return buscarPorId(dados.getId());
    }

    /**
     * Remove o equipamento do catálogo.
     * - Nunca teve reserva: é apagado de verdade do banco (DELETE).
     * - Já teve reserva: exclusão lógica (ativo = 0), para não apagar o histórico de reservas e avaliações.
     * Devolve true quando o equipamento foi apagado de verdade.
     */
    public boolean remover(Long produtorLogadoId, Long equipamentoId) throws SQLException {
        buscarDoDono(produtorLogadoId, equipamentoId);
        if (equipamentoDAO.possuiReservas(equipamentoId)) {
            equipamentoDAO.desativar(equipamentoId);
            return false;
        }
        equipamentoDAO.excluir(equipamentoId);
        return true;
    }

    private Equipamento buscarDoDono(Long produtorLogadoId, Long equipamentoId) throws SQLException {
        Equipamento equipamento = buscarPorId(equipamentoId);
        if (!equipamento.pertenceA(produtorLogadoId)) {
            throw new AcessoNegadoException("Você só pode alterar os seus próprios equipamentos");
        }
        return equipamento;
    }

    static void validar(Equipamento equipamento) {
        if (equipamento.getNome() == null || !equipamento.getNome().trim().matches("^(?=.*\\p{L}).{3,100}$")) {
            throw new ValidacaoException("Informe o nome do equipamento (pelo menos 3 caracteres, com letras)");
        }
        if (equipamento.getValorDiaria() != null && equipamento.getValorDiaria().signum() < 0) {
            throw new ValidacaoException("O valor da diária não pode ser negativo");
        }
        if (equipamento instanceof MaquinaMotorizada maquina) {
            if (maquina.getPotenciaCv() <= 0) {
                throw new ValidacaoException("A potência da máquina deve ser maior que zero");
            }
            if (maquina.getTipoCombustivel() == null) {
                throw new ValidacaoException("Informe o combustível da máquina");
            }
        } else if (equipamento instanceof ImplementoAgricola implemento) {
            if (implemento.getTipoImplemento() == null || implemento.getTipoImplemento().isBlank()) {
                throw new ValidacaoException("Informe o tipo do implemento (ex.: arado, grade, plantadeira)");
            }
        }
    }
}
