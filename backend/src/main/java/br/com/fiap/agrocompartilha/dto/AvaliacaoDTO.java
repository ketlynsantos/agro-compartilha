package br.com.fiap.agrocompartilha.dto;

import java.time.LocalDate;

public class AvaliacaoDTO {
    public Long id;
    public Long reservaId;
    public String avaliadorNome;
    public String equipamentoNome;
    public int nota;
    public String comentario;
    public LocalDate data;

    public AvaliacaoDTO() {
    }
}
