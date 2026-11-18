package br.com.fiap.agrocompartilha.dto;

import java.math.BigDecimal;

public class EquipamentoDTO {
    public Long id;
    public String tipo;
    public String nome;
    public String descricao;
    public String descricaoTecnica;
    public BigDecimal valorDiaria;
    public boolean disponivel;
    public Long donoId;
    public String donoNome;
    public String donoMunicipio;
    public double donoMediaAvaliacoes;
    public int donoTotalAvaliacoes;
    public Double potenciaCv;
    public String tipoCombustivel;
    public String tipoImplemento;
    public String compatibilidade;

    public EquipamentoDTO() {
    }
}
