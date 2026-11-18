package br.com.fiap.agrocompartilha.resource;

import br.com.fiap.agrocompartilha.dto.AvaliacaoDTO;
import br.com.fiap.agrocompartilha.dto.EquipamentoDTO;
import br.com.fiap.agrocompartilha.dto.ProdutorDTO;
import br.com.fiap.agrocompartilha.dto.ReservaDTO;
import br.com.fiap.agrocompartilha.exception.ValidacaoException;
import br.com.fiap.agrocompartilha.model.Avaliacao;
import br.com.fiap.agrocompartilha.model.Equipamento;
import br.com.fiap.agrocompartilha.model.ImplementoAgricola;
import br.com.fiap.agrocompartilha.model.MaquinaMotorizada;
import br.com.fiap.agrocompartilha.model.Produtor;
import br.com.fiap.agrocompartilha.model.Reserva;
import br.com.fiap.agrocompartilha.model.StatusReserva;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

final class Conversor {

    private Conversor() {
    }

    static ProdutorDTO produtorPublico(Produtor produtor) {
        ProdutorDTO dto = new ProdutorDTO();
        dto.id = produtor.getId();
        dto.nome = produtor.getNome();
        dto.municipio = produtor.getMunicipio();
        dto.mediaAvaliacoes = produtor.getReputacao().getMedia();
        dto.totalAvaliacoes = produtor.getReputacao().getTotal();
        return dto;
    }

    static ProdutorDTO meuPerfil(Produtor produtor) {
        ProdutorDTO dto = produtorPublico(produtor);
        dto.documento = produtor.getDocumento();
        dto.telefone = produtor.getTelefone();
        return dto;
    }

    static EquipamentoDTO equipamento(Equipamento equipamento) {
        Produtor dono = equipamento.getDono();
        EquipamentoDTO dto = new EquipamentoDTO();
        dto.id = equipamento.getId();
        dto.tipo = equipamento.getTipo().name();
        dto.nome = equipamento.getNome();
        dto.descricao = equipamento.getDescricao();
        dto.descricaoTecnica = equipamento.getDescricaoTecnica();
        dto.valorDiaria = equipamento.isGratuito() ? null : equipamento.getValorDiaria();
        dto.disponivel = equipamento.isDisponivel();
        dto.donoId = dono.getId();
        dto.donoNome = dono.getNome();
        dto.donoMunicipio = dono.getMunicipio();
        dto.donoMediaAvaliacoes = dono.getReputacao().getMedia();
        dto.donoTotalAvaliacoes = dono.getReputacao().getTotal();
        if (equipamento instanceof MaquinaMotorizada maquina) {
            dto.potenciaCv = maquina.getPotenciaCv();
            dto.tipoCombustivel = maquina.getTipoCombustivel().name();
        } else if (equipamento instanceof ImplementoAgricola implemento) {
            dto.tipoImplemento = implemento.getTipoImplemento();
            dto.compatibilidade = implemento.getCompatibilidade();
        }
        return dto;
    }

    static ReservaDTO reserva(Reserva reserva) {
        Produtor solicitante = reserva.getSolicitante();
        Produtor dono = reserva.getEquipamento().getDono();
        ReservaDTO dto = new ReservaDTO();
        dto.id = reserva.getId();
        dto.status = reserva.getStatus().name();
        dto.dataInicio = reserva.getDataInicio();
        dto.dataFim = reserva.getDataFim();
        dto.dias = reserva.getDias();
        dto.valorTotal = reserva.getValorTotal();
        dto.equipamentoId = reserva.getEquipamento().getId();
        dto.equipamentoNome = reserva.getEquipamento().getNome();
        dto.tipoEquipamento = reserva.getEquipamento().getTipo().name();
        dto.descricaoTecnica = reserva.getEquipamento().getDescricaoTecnica();
        dto.solicitanteId = solicitante.getId();
        dto.solicitanteNome = solicitante.getNome();
        dto.solicitanteMunicipio = solicitante.getMunicipio();
        dto.solicitanteMediaAvaliacoes = solicitante.getReputacao().getMedia();
        dto.solicitanteTotalAvaliacoes = solicitante.getReputacao().getTotal();
        dto.donoId = dono.getId();
        dto.donoNome = dono.getNome();
        dto.donoMediaAvaliacoes = dono.getReputacao().getMedia();
        dto.donoTotalAvaliacoes = dono.getReputacao().getTotal();
        boolean combinado = reserva.getStatus() == StatusReserva.APROVADA
            || reserva.getStatus() == StatusReserva.CONCLUIDA;
        if (combinado) {
            dto.solicitanteTelefone = solicitante.getTelefone();
            dto.donoTelefone = dono.getTelefone();
        }
        dto.avaliadaPeloSolicitante = reserva.isAvaliadaPeloSolicitante();
        dto.avaliadaPeloDono = reserva.isAvaliadaPeloDono();
        return dto;
    }

    static AvaliacaoDTO avaliacao(Avaliacao avaliacao) {
        AvaliacaoDTO dto = new AvaliacaoDTO();
        dto.id = avaliacao.getId();
        dto.reservaId = avaliacao.getReservaId();
        dto.avaliadorNome = avaliacao.getAvaliadorNome();
        dto.equipamentoNome = avaliacao.getEquipamentoNome();
        dto.nota = avaliacao.getNota();
        dto.comentario = avaliacao.getComentario();
        dto.data = avaliacao.getData();
        return dto;
    }

    static LocalDate data(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("Informe a " + campo);
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new ValidacaoException("A " + campo + " deve estar no formato AAAA-MM-DD");
        }
    }

    static <E extends Enum<E>> E enumeracao(Class<E> tipo, String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("Informe o campo " + campo);
        }
        try {
            return Enum.valueOf(tipo, texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("Valor inválido para " + campo + ": " + texto);
        }
    }

    static void exigirCorpo(Object corpo) {
        if (corpo == null) {
            throw new ValidacaoException("O corpo da requisição está vazio");
        }
    }

    static String aparar(String texto) {
        return texto == null ? null : texto.trim();
    }
}
