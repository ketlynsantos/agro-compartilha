package br.com.fiap.agrocompartilha.model;

import java.math.BigDecimal;

public class ImplementoAgricola extends Equipamento {

    private String tipoImplemento;
    private String compatibilidade;

    public ImplementoAgricola(Produtor dono, String nome, String descricao, BigDecimal valorDiaria,
                              String tipoImplemento, String compatibilidade) {
        super(dono, nome, descricao, valorDiaria);
        this.tipoImplemento = tipoImplemento;
        this.compatibilidade = compatibilidade;
    }

    @Override
    public TipoEquipamento getTipo() {
        return TipoEquipamento.IMPLEMENTO;
    }

    @Override
    public String getDescricaoTecnica() {
        if (compatibilidade == null || compatibilidade.isBlank()) {
            return tipoImplemento + " · precisa de trator";
        }
        return tipoImplemento + " · compatível com " + compatibilidade;
    }

    public String getTipoImplemento() { return tipoImplemento; }
    public void setTipoImplemento(String tipoImplemento) { this.tipoImplemento = tipoImplemento; }
    public String getCompatibilidade() { return compatibilidade; }
    public void setCompatibilidade(String compatibilidade) { this.compatibilidade = compatibilidade; }
}
