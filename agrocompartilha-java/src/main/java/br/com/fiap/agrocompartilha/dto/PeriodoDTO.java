package br.com.fiap.agrocompartilha.dto;

import java.time.LocalDate;

public class PeriodoDTO {
    public LocalDate dataInicio;
    public LocalDate dataFim;

    public PeriodoDTO() {
    }

    public PeriodoDTO(LocalDate dataInicio, LocalDate dataFim) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }
}
