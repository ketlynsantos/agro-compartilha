package br.com.fiap.agrocompartilha.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Pedido de um produtor para usar o equipamento de outro em um intervalo de datas.
 * Liga Produtor (solicitante) e Equipamento: é a associação N:N do domínio.
 *
 * Ciclo de vida:
 *   PENDENTE -> APROVADA -> CONCLUIDA
 *   PENDENTE -> REJEITADA
 *   PENDENTE ou APROVADA (antes do início) -> CANCELADA
 * Cada mudança de status é feita por um método desta classe, que barra as transições inválidas.
 */
public class Reserva {

    private Long id;
    private final Produtor solicitante;
    private final Equipamento equipamento;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private StatusReserva status;
    private BigDecimal valorTotal;
    private boolean avaliadaPeloSolicitante;
    private boolean avaliadaPeloDono;

    public Reserva(Produtor solicitante, Equipamento equipamento, LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias");
        }
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de fim não pode ser anterior à data de início");
        }
        this.solicitante = solicitante;
        this.equipamento = equipamento;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = StatusReserva.PENDENTE;
    }

    public long getDias() {
        return ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
    }

    /** O valor é fixado no pedido: se o dono mudar a diária depois, este pedido não muda. */
    public void calcularValorTotal() {
        this.valorTotal = equipamento.calcularValor(getDias());
    }

    public boolean sobrepoe(Reserva outra) {
        return !(dataFim.isBefore(outra.dataInicio) || dataInicio.isAfter(outra.dataFim));
    }

    public void aprovar(LocalDate hoje) {
        exigirStatus(StatusReserva.PENDENTE, "aprovar");
        if (dataInicio.isBefore(hoje)) {
            throw new IllegalStateException("Este pedido expirou: a data de início já passou");
        }
        status = StatusReserva.APROVADA;
    }

    public void rejeitar() {
        exigirStatus(StatusReserva.PENDENTE, "rejeitar");
        status = StatusReserva.REJEITADA;
    }

    public void cancelar(LocalDate hoje) {
        if (status != StatusReserva.PENDENTE && status != StatusReserva.APROVADA) {
            throw new IllegalStateException("Só é possível cancelar reservas pendentes ou aprovadas");
        }
        if (status == StatusReserva.APROVADA && !dataInicio.isAfter(hoje)) {
            throw new IllegalStateException("Não é possível cancelar uma reserva aprovada no dia ou depois do início");
        }
        status = StatusReserva.CANCELADA;
    }

    public void concluir(LocalDate hoje) {
        exigirStatus(StatusReserva.APROVADA, "concluir");
        if (hoje.isBefore(dataInicio)) {
            throw new IllegalStateException("Só é possível concluir depois que o empréstimo começou");
        }
        status = StatusReserva.CONCLUIDA;
    }

    public boolean envolve(Long produtorId) {
        return solicitante.getId().equals(produtorId) || equipamento.pertenceA(produtorId);
    }

    public Produtor outraParte(Long produtorId) {
        return solicitante.getId().equals(produtorId) ? equipamento.getDono() : solicitante;
    }

    private void exigirStatus(StatusReserva esperado, String acao) {
        if (status != esperado) {
            throw new IllegalStateException("Só é possível " + acao + " uma reserva " + esperado.name().toLowerCase()
                + " (esta está " + status.name().toLowerCase() + ")");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Produtor getSolicitante() { return solicitante; }
    public Equipamento getEquipamento() { return equipamento; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public StatusReserva getStatus() { return status; }
    public BigDecimal getValorTotal() { return valorTotal; }

    public void setStatus(StatusReserva status) { this.status = status; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public boolean isAvaliadaPeloSolicitante() { return avaliadaPeloSolicitante; }
    public void setAvaliadaPeloSolicitante(boolean avaliada) { this.avaliadaPeloSolicitante = avaliada; }
    public boolean isAvaliadaPeloDono() { return avaliadaPeloDono; }
    public void setAvaliadaPeloDono(boolean avaliada) { this.avaliadaPeloDono = avaliada; }
}
