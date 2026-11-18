package br.com.fiap.agrocompartilha.model;

import java.time.LocalDate;

public class Avaliacao {

    private Long id;
    private final Long reservaId;
    private final Long avaliadorId;
    private final Long avaliadoId;
    private final int nota;
    private final String comentario;
    private final LocalDate data;
    private String avaliadorNome;
    private String equipamentoNome;

    public Avaliacao(Long reservaId, Long avaliadorId, Long avaliadoId, int nota, String comentario) {
        this(reservaId, avaliadorId, avaliadoId, nota, comentario, LocalDate.now());
    }

    public Avaliacao(Long reservaId, Long avaliadorId, Long avaliadoId, int nota, String comentario, LocalDate data) {
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("A nota deve estar entre 1 e 5");
        }
        this.reservaId = reservaId;
        this.avaliadorId = avaliadorId;
        this.avaliadoId = avaliadoId;
        this.nota = nota;
        this.comentario = comentario;
        this.data = data;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReservaId() { return reservaId; }
    public Long getAvaliadorId() { return avaliadorId; }
    public Long getAvaliadoId() { return avaliadoId; }
    public int getNota() { return nota; }
    public String getComentario() { return comentario; }
    public LocalDate getData() { return data; }
    public String getAvaliadorNome() { return avaliadorNome; }
    public void setAvaliadorNome(String avaliadorNome) { this.avaliadorNome = avaliadorNome; }
    public String getEquipamentoNome() { return equipamentoNome; }
    public void setEquipamentoNome(String equipamentoNome) { this.equipamentoNome = equipamentoNome; }
}
