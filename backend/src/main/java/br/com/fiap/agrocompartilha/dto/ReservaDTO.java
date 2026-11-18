package br.com.fiap.agrocompartilha.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReservaDTO {
    public Long id;
    public String status;
    public LocalDate dataInicio;
    public LocalDate dataFim;
    public long dias;
    public BigDecimal valorTotal;
    public Long equipamentoId;
    public String equipamentoNome;
    public String tipoEquipamento;
    public String descricaoTecnica;
    public Long solicitanteId;
    public String solicitanteNome;
    public String solicitanteMunicipio;
    public String solicitanteTelefone;
    public double solicitanteMediaAvaliacoes;
    public int solicitanteTotalAvaliacoes;
    public Long donoId;
    public String donoNome;
    public String donoTelefone;
    public double donoMediaAvaliacoes;
    public int donoTotalAvaliacoes;
    public boolean avaliadaPeloSolicitante;
    public boolean avaliadaPeloDono;

    public ReservaDTO() {
    }
}
