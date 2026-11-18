package br.com.fiap.agrocompartilha.model;

public class Produtor {

    private Long id;
    private String nome;
    private String documento;
    private String telefone;
    private String municipio;
    private String senhaHash;
    private Reputacao reputacao = Reputacao.semAvaliacoes();

    public Produtor(String nome, String documento, String telefone, String municipio) {
        this.nome = nome;
        this.documento = documento;
        this.telefone = telefone;
        this.municipio = municipio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public Reputacao getReputacao() { return reputacao; }
    public void setReputacao(Reputacao reputacao) { this.reputacao = reputacao; }
}
