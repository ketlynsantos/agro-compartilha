let equipamentos = [];
let equipamentoSelecionado = null;
let periodosOcupados = [];        // datas aprovadas do equipamento aberto no modal
let pedidosPorEquipamento = {};   // pedidos ativos do usuário logado, por id do equipamento

const lista = document.getElementById("lista-equipamentos");
const filtroBusca = document.getElementById("filtro-busca");
const filtroTipo = document.getElementById("filtro-tipo");
const filtroPreco = document.getElementById("filtro-preco");

const modal = document.getElementById("modal-reserva");
const modalResumo = document.getElementById("modal-resumo");
const formReserva = document.getElementById("form-reserva");
const campoInicio = document.getElementById("inicio-reserva");
const campoFim = document.getElementById("fim-reserva");
const resumoValor = document.getElementById("resumo-valor");
const botaoConfirmar = document.getElementById("modal-confirmar");
const modalOcupacao = document.getElementById("modal-ocupacao");
const avisoConflito = document.getElementById("aviso-conflito");

async function carregarCatalogo() {
  try {
    const resultado = await Promise.all([apiGet("/equipamentos"), carregarMeusPedidos()]);
    equipamentos = resultado[0];
    renderizarCatalogo();
  } catch (erro) {
    lista.innerHTML = '<p class="vazio">Não foi possível carregar o catálogo.</p>';
    mostrarMensagem(erro.message, "erro");
  }
}

async function carregarMeusPedidos() {
  pedidosPorEquipamento = {};
  if (!getSessao()) {
    return;
  }
  try {
    const minhas = await apiGet("/reservas/minhas", true);
    const hoje = hojeIso();
    minhas
      .filter(function (r) {
        return r.status === "PENDENTE" || (r.status === "APROVADA" && r.dataFim >= hoje);
      })
      .forEach(function (r) {
        (pedidosPorEquipamento[r.equipamentoId] = pedidosPorEquipamento[r.equipamentoId] || []).push(r);
      });
  } catch (erro) {
    pedidosPorEquipamento = {};
  }
}

function blocoMeusPedidos(equipamentoId) {
  const pedidos = pedidosPorEquipamento[equipamentoId];
  if (!pedidos || pedidos.length === 0) {
    return "";
  }
  return (
    '<div class="meus-pedidos">' +
      pedidos.map(function (r) {
        return '<p class="meu-pedido">' + pillStatus(r.status) +
          '<span>Seu pedido: <span class="data">' + formatarData(r.dataInicio) + " a " + formatarData(r.dataFim) +
          "</span></span></p>";
      }).join("") +
      '<a class="link-meus-pedidos" href="minhas-reservas.html">Ver em Minhas reservas</a>' +
    "</div>"
  );
}

function renderizarCatalogo() {
  const busca = filtroBusca.value.trim().toLowerCase();
  const tipo = filtroTipo.value;
  const soGratuitos = filtroPreco.checked;

  const filtrados = equipamentos.filter(function (e) {
    const texto = (e.nome + " " + e.donoMunicipio + " " + (e.descricao || "")).toLowerCase();
    return (!busca || texto.includes(busca))
      && (!tipo || e.tipo === tipo)
      && (!soGratuitos || !e.valorDiaria);
  });

  if (filtrados.length === 0) {
    lista.innerHTML = '<p class="vazio">Nenhum equipamento encontrado com esses filtros.</p>';
    return;
  }
  lista.innerHTML = filtrados.map(cartaoEquipamento).join("");
}

function cartaoEquipamento(e) {
  const sessao = getSessao();
  const meu = sessao && e.donoId === sessao.id;

  let acao;
  if (meu) {
    acao = '<p class="dica">Este equipamento é seu.</p>';
  } else if (!e.disponivel) {
    acao = '<p class="dica">O dono pausou os empréstimos por enquanto.</p>';
  } else {
    const jaPediu = pedidosPorEquipamento[e.id] && pedidosPorEquipamento[e.id].length > 0;
    acao = '<button type="button" class="' + (jaPediu ? "botao botao-secundario" : "botao") + '" ' +
      'onclick="abrirReserva(' + e.id + ')">' + (jaPediu ? "Pedir outras datas" : "Quero reservar") + "</button>";
  }

  return (
    '<article class="cartao" id="equipamento-' + e.id + '">' +
      '<div class="cartao-topo">' +
        "<h3>" + escaparHtml(e.nome) + "</h3>" +
        (e.disponivel ? "" : pillPausado()) +
      "</div>" +
      blocoPreco(e.valorDiaria, "/dia") +
      fichaTecnica(e.tipo, e.descricaoTecnica) +
      (e.descricao ? '<p class="cartao-descricao">' + escaparHtml(e.descricao) + "</p>" : "") +
      '<div class="cartao-rodape">' +
        "<span><strong>" + escaparHtml(e.donoNome) + "</strong> · " + escaparHtml(e.donoMunicipio) + "</span>" +
        blocoReputacao(e.donoId, e.donoMediaAvaliacoes, e.donoTotalAvaliacoes, e.donoNome) +
      "</div>" +
      blocoMeusPedidos(e.id) +
      '<div class="cartao-acoes">' + acao + "</div>" +
    "</article>"
  );
}

function abrirReserva(id) {
  limparMensagem();
  if (!getSessao()) {
    irParaLogin();
    return;
  }

  const e = equipamentos.find(function (item) { return item.id === id; });
  if (!e) {
    return;
  }
  equipamentoSelecionado = e;

  modalResumo.innerHTML =
    "<h3>" + escaparHtml(e.nome) + "</h3>" +
    blocoPreco(e.valorDiaria, "/dia") +
    '<p class="modal-dono"><span><strong>' + escaparHtml(e.donoNome) + "</strong> · " +
      escaparHtml(e.donoMunicipio) + "</span>" +
      blocoReputacao(e.donoId, e.donoMediaAvaliacoes, e.donoTotalAvaliacoes, e.donoNome) +
    "</p>";

  formReserva.reset();
  limparErros(formReserva);
  limparMensagem("mensagem-modal");
  campoInicio.min = hojeIso();
  campoFim.min = hojeIso();
  resumoValor.textContent = "";
  avisoConflito.hidden = true;
  botaoConfirmar.disabled = false;
  periodosOcupados = [];
  modalOcupacao.innerHTML = '<p class="dica">Consultando datas já reservadas...</p>';

  modal.showModal();
  campoInicio.focus();
  carregarOcupacao(e.id);
}

async function carregarOcupacao(equipamentoId) {
  try {
    const periodos = await apiGet("/equipamentos/" + equipamentoId + "/ocupacao", true);
    if (!equipamentoSelecionado || equipamentoSelecionado.id !== equipamentoId) {
      return; // o usuário já fechou ou trocou de equipamento
    }
    periodosOcupados = periodos;
    renderizarOcupacao();
    verificarConflito();
  } catch (erro) {
    modalOcupacao.innerHTML = '<p class="dica">Não foi possível consultar as datas reservadas agora.</p>';
  }
}

function renderizarOcupacao() {
  const listaPeriodos = function (periodos) {
    return periodos.map(function (p) {
      return '<li class="data">' + formatarData(p.dataInicio) + " a " + formatarData(p.dataFim) + "</li>";
    }).join("");
  };

  let html = periodosOcupados.length
    ? '<p class="modal-ocupacao-titulo">' + icone("calendar_month") + "Datas já reservadas</p>" +
      '<ul class="lista-periodos">' + listaPeriodos(periodosOcupados) + "</ul>"
    : '<p class="modal-ocupacao-titulo">' + icone("calendar_month") + "Nenhuma data reservada até agora.</p>";

  const meus = pedidosPorEquipamento[equipamentoSelecionado.id] || [];
  const meusPendentes = meus.filter(function (r) { return r.status === "PENDENTE"; });
  if (meusPendentes.length) {
    html += '<p class="dica">Você já tem ' + (meusPendentes.length === 1 ? "um pedido pendente" : meusPendentes.length + " pedidos pendentes") +
      " deste equipamento: " + meusPendentes.map(function (r) {
        return formatarData(r.dataInicio) + " a " + formatarData(r.dataFim);
      }).join(", ") + ".</p>";
  }
  modalOcupacao.innerHTML = html;
}

function verificarConflito() {
  const inicio = campoInicio.value;
  const fim = campoFim.value;
  const conflito = inicio && fim && fim >= inicio && periodosOcupados.find(function (p) {
    return periodosSobrepostos(inicio, fim, p.dataInicio, p.dataFim);
  });
  if (conflito) {
    avisoConflito.textContent = "Esse período bate com uma reserva de " + formatarData(conflito.dataInicio) +
      " a " + formatarData(conflito.dataFim) + ". Escolha outras datas.";
    avisoConflito.hidden = false;
  } else {
    avisoConflito.hidden = true;
  }
  return Boolean(conflito);
}

function fecharReserva() {
  modal.close();
  equipamentoSelecionado = null;
}

modal.addEventListener("close", function () {
  equipamentoSelecionado = null;
});

// Mostra o valor enquanto o usuário escolhe as datas (o servidor recalcula e fixa no pedido).
function atualizarResumo() {
  const e = equipamentoSelecionado;
  const inicio = campoInicio.value;
  const fim = campoFim.value;
  if (!e || !inicio || !fim || fim < inicio) {
    resumoValor.textContent = "";
    return;
  }
  const dias = Math.round((new Date(fim) - new Date(inicio)) / 86400000) + 1;
  const total = (Number(e.valorDiaria) || 0) * dias;
  resumoValor.textContent = dias + (dias === 1 ? " dia" : " dias") + " · " +
    (total > 0 ? "Total: " + formatarMoeda(total) : "Sem custo");
}

async function enviarReserva(evento) {
  evento.preventDefault();
  limparErros(formReserva);
  limparMensagem("mensagem-modal");

  if (!campoInicio.value) {
    marcarErro(campoInicio, "Escolha a data de início.");
  } else if (campoInicio.value < hojeIso()) {
    marcarErro(campoInicio, "A data de início não pode estar no passado.");
  }
  if (!campoFim.value) {
    marcarErro(campoFim, "Escolha a data de fim.");
  } else if (campoInicio.value && campoFim.value < campoInicio.value) {
    marcarErro(campoFim, "A data de fim deve ser igual ou depois do início.");
  }
  if (formReserva.querySelector(".invalido")) {
    focarPrimeiroErro(formReserva);
    return;
  }
  if (verificarConflito()) {
    campoInicio.focus();
    return;
  }

  botaoConfirmar.disabled = true;
  try {
    await apiPost("/reservas", {
      equipamentoId: equipamentoSelecionado.id,
      dataInicio: campoInicio.value,
      dataFim: campoFim.value
    });
    const nome = equipamentoSelecionado.nome;
    fecharReserva();
    mostrarMensagem("Pedido de \"" + nome + "\" enviado! Agora é só aguardar o dono aprovar. " +
      "Acompanhe em \"Minhas reservas\".", "sucesso");
    await carregarMeusPedidos();
    renderizarCatalogo();
  } catch (erro) {
    mostrarMensagem(erro.message, "erro", "mensagem-modal");
    botaoConfirmar.disabled = false;
  }
}

campoInicio.addEventListener("change", function () {
  campoFim.min = campoInicio.value || hojeIso();
  atualizarResumo();
  verificarConflito();
});
campoFim.addEventListener("change", function () {
  atualizarResumo();
  verificarConflito();
});
formReserva.addEventListener("submit", enviarReserva);
document.getElementById("modal-fechar").addEventListener("click", fecharReserva);
document.getElementById("modal-cancelar").addEventListener("click", fecharReserva);

modal.addEventListener("click", function (evento) {
  if (evento.target === modal) {
    fecharReserva();
  }
});

document.getElementById("form-filtros").addEventListener("submit", function (evento) {
  evento.preventDefault();
});
filtroBusca.addEventListener("input", renderizarCatalogo);
filtroTipo.addEventListener("change", renderizarCatalogo);
filtroPreco.addEventListener("change", renderizarCatalogo);

carregarCatalogo();
