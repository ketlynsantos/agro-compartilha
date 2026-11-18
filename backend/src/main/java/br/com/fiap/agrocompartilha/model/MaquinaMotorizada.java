package br.com.fiap.agrocompartilha.model;

import java.math.BigDecimal;

public class MaquinaMotorizada extends Equipamento {

    private double potenciaCv;
    private TipoCombustivel tipoCombustivel;

    public MaquinaMotorizada(Produtor dono, String nome, String descricao, BigDecimal valorDiaria,
                             double potenciaCv, TipoCombustivel tipoCombustivel) {
        super(dono, nome, descricao, valorDiaria);
        this.potenciaCv = potenciaCv;
        this.tipoCombustivel = tipoCombustivel;
    }

    @Override
    public TipoEquipamento getTipo() {
        return TipoEquipamento.MOTORIZADA;
    }

    @Override
    public String getDescricaoTecnica() {
        String combustivel = tipoCombustivel == TipoCombustivel.DIESEL ? "diesel" : "gasolina";
        return String.format("%.0f cv · movido a %s · devolver com o mesmo nível de combustível",
            potenciaCv, combustivel);
    }

    public double getPotenciaCv() { return potenciaCv; }
    public void setPotenciaCv(double potenciaCv) { this.potenciaCv = potenciaCv; }
    public TipoCombustivel getTipoCombustivel() { return tipoCombustivel; }
    public void setTipoCombustivel(TipoCombustivel tipoCombustivel) { this.tipoCombustivel = tipoCombustivel; }
}
