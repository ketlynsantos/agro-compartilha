package br.com.fiap.agrocompartilha.dto;

import java.math.BigDecimal;

public class NovoEquipamentoRequest {
    public String tipo;
    public String nome;
    public String descricao;
    public BigDecimal valorDiaria;
    public Boolean disponivel;
    public Double potenciaCv;
    public String tipoCombustivel;
    public String tipoImplemento;
    public String compatibilidade;

    public NovoEquipamentoRequest() {
    }
}
