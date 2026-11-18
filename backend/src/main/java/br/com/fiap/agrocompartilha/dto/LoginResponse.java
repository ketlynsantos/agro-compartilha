package br.com.fiap.agrocompartilha.dto;

public class LoginResponse {
    public String token;
    public ProdutorDTO produtor;

    public LoginResponse() {
    }

    public LoginResponse(String token, ProdutorDTO produtor) {
        this.token = token;
        this.produtor = produtor;
    }
}
