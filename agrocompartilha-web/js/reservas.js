const papel = document.body.dataset.papel;
const souDono = papel === "dono";
const lista = document.getElementById("lista-reservas");
const filtroStatus = document.getElementById("filtro-status");
let reservas = [];

async function carregarReservas() {
  try {
    reservas = await apiGet(souDono ? "/reservas/recebidas" : "/reservas/minhas");
    renderizarReservas();
  } catch (erro) {
    lista.innerHTML = '<p class="vazio">Não foi possível carregar as reservas.</p>';
    mostrarMensagem(erro.message, "erro");
  }
}

function renderizarReservas() {
  const status = filtroStatus.value;
  const filtradas = reservas.filter(function (r) { return !status || r.status === status; });
  if (filtradas.length === 0) {
    lista.innerHTML = souDono
      ? '<p class="vazio">Nenhum pedido para os seus equipamentos.</p>'
      : '<p class="vazio">Você ainda não pediu nenhum equipamento. Veja o <a href="index.html">catálogo</a>.</p>';
    return;
  }
  lista.innerHTML = filtradas.map(renderReserva).join("");
}

function renderReserva(r) {
  return (
    '<article class="cartao" id="reserva-' + r.id + '">' +
      '<div class="cartao-topo">' +
        "<h3>" + escaparHtml(r.equipamentoNome) + "</h3>" +
        pillStatus(r.status) +
      "</div>" +
      blocoPreco(r.valorTotal, "no total") +
      '<p class="linha-info">' + icone("calendar_month") + '<span class="data">' + formatarData(r.dataInicio) +
        " a " + formatarData(r.dataFim) + " · " + r.dias + (r.dias === 1 ? " dia" : " dias") + "</span></p>" +
      fichaTecnica(r.tipoEquipamento, r.descricaoTecnica) +
      blocoOutraParte(r) +
      '<div class="cartao-acoes">' + botoesDaReserva(r) + "</div>" +
    "</article>"
  );
}

function blocoOutraParte(r) {
  if (souDono) {
    return (
      '<div class="reputacao-solicitante">' +
        '<div class="linha"><span>Pedido por <strong>' + escaparHtml(r.solicitanteNome) + "</strong> · " +
          escaparHtml(r.solicitanteMunicipio) + "</span>" +
          blocoReputacao(r.solicitanteId, r.solicitanteMediaAvaliacoes, r.solicitanteTotalAvaliacoes, r.solicitanteNome) +
        "</div>" +
        (r.solicitanteTelefone
          ? '<div class="linha">' + icone("call") + '<strong class="numero">' + formatarTelefone(r.solicitanteTelefone) + "</strong></div>"
          : "") +
      "</div>"
    );
  }
  return (
    '<div class="reputacao-solicitante">' +
      '<div class="linha"><span>Dono: <strong>' + escaparHtml(r.donoNome) + "</strong></span>" +
        blocoReputacao(r.donoId, r.donoMediaAvaliacoes, r.donoTotalAvaliacoes, r.donoNome) +
      "</div>" +
      (r.donoTelefone
        ? '<div class="linha">' + icone("call") + '<strong class="numero">' + formatarTelefone(r.donoTelefone) + "</strong></div>"
        : '<p class="dica">O telefone do dono aparece aqui quando o pedido for aprovado.</p>') +
    "</div>"
  );
}

function botoesDaReserva(r) {
  const botoes = [];
  const hoje = hojeIso();
  if (souDono) {
    if (r.status === "PENDENTE") {
      botoes.push(botao("Aprovar", "botao", "mudarStatus(" + r.id + ", 'aprovar')"));
      botoes.push(botao("Rejeitar", "botao botao-perigo", "mudarStatus(" + r.id + ", 'rejeitar')"));
    }
    if (r.status === "APROVADA") {
      botoes.push(r.dataInicio <= hoje
        ? botao("Marcar como devolvido", "botao", "mudarStatus(" + r.id + ", 'concluir')")
        : '<span class="dica">Retirada combinada para <span class="data">' + formatarData(r.dataInicio) + "</span>.</span>");
    }
    if (r.status === "CONCLUIDA") {
      botoes.push(r.avaliadaPeloDono
        ? '<span class="dica">Você já avaliou quem pegou.</span>'
        : botao("Avaliar quem pegou", "botao botao-secundario", "abrirAvaliacao(" + r.id + ")"));
    }
  } else {
    const podeCancelar = r.status === "PENDENTE" || (r.status === "APROVADA" && r.dataInicio > hoje);
    if (podeCancelar) {
      botoes.push(botao("Cancelar pedido", "botao botao-perigo", "mudarStatus(" + r.id + ", 'cancelar')"));
    }
    if (r.status === "CONCLUIDA") {
      botoes.push(r.avaliadaPeloSolicitante
        ? '<span class="dica">Você já avaliou o dono.</span>'
        : botao("Avaliar o dono", "botao botao-secundario", "abrirAvaliacao(" + r.id + ")"));
    }
  }
  return botoes.join("");
}

function botao(texto, classes, acao) {
  return '<button type="button" class="' + classes + ' botao-pequeno" onclick="' + acao + '">' + texto + "</button>";
}

const CONFIRMACOES = {
  aprovar: "Aprovar este pedido? Outros pedidos com datas em conflito não poderão mais ser aprovados.",
  rejeitar: "Rejeitar este pedido?",
  cancelar: "Cancelar este pedido?",
  concluir: "Confirmar que o equipamento foi devolvido (com o mesmo nível de combustível, se for máquina)?"
};

const SUCESSOS = {
  aprovar: "Reserva aprovada! O telefone de quem pediu já aparece no cartão para vocês combinarem a retirada.",
  rejeitar: "Pedido rejeitado.",
  cancelar: "Pedido cancelado.",
  concluir: "Equipamento devolvido. Que tal avaliar quem pegou?"
};

async function mudarStatus(id, acao) {
  if (!confirm(CONFIRMACOES[acao])) {
    return;
  }
  limparMensagem();
  try {
    await apiPut("/reservas/" + id + "/" + acao);
    mostrarMensagem(SUCESSOS[acao], "sucesso");
    await carregarReservas();
  } catch (erro) {
    mostrarMensagem(erro.message, "erro");
  }
}

let reservaEmAvaliacao = null;

const modalAvaliar = criarModal("modal-avaliar",
  '<button type="button" class="modal-fechar" data-fechar aria-label="Fechar">✕</button>' +
  '<h2 id="modal-avaliar-titulo">Avaliar</h2>' +
  '<div class="modal-resumo" id="modal-avaliar-resumo"></div>' +
  '<div id="mensagem-avaliacao" class="mensagem" role="status" aria-live="polite"></div>' +
  '<form id="form-avaliacao" novalidate>' +
    '<div class="campo">' +
      '<label for="nota-avaliacao">Nota</label>' +
      '<select id="nota-avaliacao">' +
        '<option value="">Escolha de 1 a 5</option>' +
        '<option value="5">★★★★★ Excelente</option>' +
        '<option value="4">★★★★ Bom</option>' +
        '<option value="3">★★★ Regular</option>' +
        '<option value="2">★★ Ruim</option>' +
        '<option value="1">★ Péssimo</option>' +
      "</select>" +
      '<span class="erro-campo" id="erro-nota-avaliacao"></span>' +
    "</div>" +
    '<div class="campo">' +
      '<label for="comentario-avaliacao">Comentário</label>' +
      '<textarea id="comentario-avaliacao" rows="3" maxlength="500" ' +
        'placeholder="Pontualidade, cuidado com o equipamento, combustível..."></textarea>' +
      '<span class="dica">Obrigatório para notas 1 ou 2.</span>' +
      '<span class="erro-campo" id="erro-comentario-avaliacao"></span>' +
    "</div>" +
    '<div class="modal-acoes">' +
      '<button type="button" class="botao botao-secundario" data-fechar>Cancelar</button>' +
      '<button type="submit" class="botao" id="botao-enviar-avaliacao">Enviar avaliação</button>' +
    "</div>" +
  "</form>");

const formAvaliacao = document.getElementById("form-avaliacao");
const campoNota = document.getElementById("nota-avaliacao");
const campoComentario = document.getElementById("comentario-avaliacao");
const botaoEnviarAvaliacao = document.getElementById("botao-enviar-avaliacao");

function abrirAvaliacao(id) {
  const r = reservas.find(function (item) { return item.id === id; });
  if (!r) {
    return;
  }
  reservaEmAvaliacao = r;

  const avaliado = souDono ? r.solicitanteNome : r.donoNome;
  document.getElementById("modal-avaliar-titulo").textContent = souDono ? "Avaliar quem pegou" : "Avaliar o dono";
  document.getElementById("modal-avaliar-resumo").innerHTML =
    "<h3>" + escaparHtml(avaliado) + "</h3>" +
    '<p class="dica">' + escaparHtml(r.equipamentoNome) + ' · <span class="data">' +
      formatarData(r.dataInicio) + " a " + formatarData(r.dataFim) + "</span></p>";

  formAvaliacao.reset();
  limparErros(formAvaliacao);
  limparMensagem("mensagem-avaliacao");
  botaoEnviarAvaliacao.disabled = false;

  modalAvaliar.showModal();
  campoNota.focus();
}

modalAvaliar.addEventListener("close", function () {
  reservaEmAvaliacao = null;
});

formAvaliacao.addEventListener("submit", async function (evento) {
  evento.preventDefault();
  limparErros(formAvaliacao);
  limparMensagem("mensagem-avaliacao");

  if (!campoNota.value) {
    marcarErro(campoNota, "Escolha uma nota.");
  } else if (Number(campoNota.value) <= 2 && campoComentario.value.trim().length < 5) {
    marcarErro(campoComentario, "Para notas 1 ou 2, conte o que aconteceu (pelo menos 5 letras).");
  }
  if (formAvaliacao.querySelector(".invalido")) {
    focarPrimeiroErro(formAvaliacao);
    return;
  }

  botaoEnviarAvaliacao.disabled = true;
  try {
    // Quem avalia é quem está logado e quem é avaliado o servidor descobre pela reserva.
    await apiPost("/avaliacoes", {
      reservaId: reservaEmAvaliacao.id,
      nota: Number(campoNota.value),
      comentario: campoComentario.value.trim()
    });
    modalAvaliar.close();
    mostrarMensagem("Obrigado pela avaliação! Ela ajuda toda a comunidade.", "sucesso");
    await carregarReservas();
  } catch (erro) {
    mostrarMensagem(erro.message, "erro", "mensagem-avaliacao");
    botaoEnviarAvaliacao.disabled = false;
  }
});

filtroStatus.addEventListener("change", renderizarReservas);

if (exigirLogin()) {
  carregarReservas();
}
