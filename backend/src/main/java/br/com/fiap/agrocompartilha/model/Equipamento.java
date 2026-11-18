package br.com.fiap.agrocompartilha.model;

import java.math.BigDecimal;

public abstract class Equipamento {

    private Long id;
    private Produtor dono;
    private String nome;
    private String descricao;
    private BigDecimal valorDiaria;
    private boolean disponivel = true;
    private boolean ativo = true;

    protected Equipamento(Produtor dono, String nome, String descricao, BigDecimal valorDiaria) {
        this.dono = dono;
        this.nome = nome;
        this.descricao = descricao;
        this.valorDiaria = valorDiaria;
    }

    public abstract TipoEquipamento getTipo();

    public abstract String getDescricaoTecnica();

    public boolean isGratuito() {
        return valorDiaria == null || valorDiaria.signum() == 0;
    }

    public BigDecimal calcularValor(long dias) {
        if (isGratuito()) {
            return BigDecimal.ZERO;
        }
        return valorDiaria.multiply(BigDecimal.valueOf(dias));
    }

    public boolean podeSerReservado() {
        return ativo && disponivel;
    }

    public boolean pertenceA(Long produtorId) {
        return dono != null && dono.getId() != null && dono.getId().equals(produtorId);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Produtor getDono() { return dono; }
    public void setDono(Produtor dono) { this.dono = dono; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValorDiaria() { return valorDiaria; }
    public void setValorDiaria(BigDecimal valorDiaria) { this.valorDiaria = valorDiaria; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
