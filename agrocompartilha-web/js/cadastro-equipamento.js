let meusEquipamentos = [];

const formulario = document.getElementById("form-equipamento");
const campoId = document.getElementById("equipamento-id");
const campoTipo = document.getElementById("tipo");
const campoNome = document.getElementById("nome");
const campoDescricao = document.getElementById("descricao");
const campoValor = document.getElementById("valor-diaria");
const campoDisponivel = document.getElementById("disponivel");
const campoPotencia = document.getElementById("potencia");
const campoCombustivel = document.getElementById("combustivel");
const campoTipoImplemento = document.getElementById("tipo-implemento");
const campoCompatibilidade = document.getElementById("compatibilidade");
const blocoMotorizada = document.getElementById("campos-motorizada");
const blocoImplemento = document.getElementById("campos-implemento");
const botaoSalvar = document.getElementById("botao-salvar");
const botaoCancelar = document.getElementById("botao-cancelar");
const tituloForm = document.getElementById("titulo-form");
const lista = document.getElementById("lista-equipamentos");

function alternarCamposDoTipo() {
  const motorizada = campoTipo.value === "MOTORIZADA";
  blocoMotorizada.hidden = !motorizada;
  blocoImplemento.hidden = motorizada;
}

async function carregarMeusEquipamentos() {
  try {
    meusEquipamentos = await apiGet("/equipamentos/meus");
    renderizarLista();
  } catch (erro) {
    lista.innerHTML = '<p class="vazio">Não foi possível carregar seus equipamentos.</p>';
    mostrarMensagem(erro.message, "erro");
  }
}

function renderizarLista() {
  if (meusEquipamentos.length === 0) {
    lista.innerHTML = '<p class="vazio">Você ainda não cadastrou nenhum equipamento.</p>';
    return;
  }
  lista.innerHTML = meusEquipamentos.map(function (e) {
    return (
      '<article class="cartao">' +
        '<div class="cartao-topo">' +
          "<h3>" + escaparHtml(e.nome) + "</h3>" +
          (e.disponivel ? "" : pillPausado()) +
        "</div>" +
        blocoPreco(e.valorDiaria, "/dia") +
        fichaTecnica(e.tipo, e.descricaoTecnica) +
        // O aria-label diz qual equipamento, já que todos os cartões têm botões com o mesmo texto
        '<div class="cartao-acoes acoes-lado-a-lado">' +
          '<button type="button" class="botao botao-secundario botao-pequeno" onclick="editarEquipamento(' + e.id + ')" ' +
            'aria-label="Editar ' + escaparHtml(e.nome) + '">Editar</button>' +
          '<button type="button" class="botao botao-perigo botao-pequeno" onclick="removerEquipamento(' + e.id + ')" ' +
            'aria-label="Remover ' + escaparHtml(e.nome) + ' do catálogo">Remover</button>' +
        "</div>" +
      "</article>"
    );
  }).join("");
}

function validarFormulario() {
  limparErros(formulario);

  const nome = campoNome.value.trim();
  if (nome.length < 3 || !REGEX.temLetra.test(nome)) {
    marcarErro(campoNome, "Informe o nome do equipamento (pelo menos 3 caracteres, com letras).");
  }
  if (campoValor.value !== "" && Number(campoValor.value) < 0) {
    marcarErro(campoValor, "A diária não pode ser negativa.");
  }
  if (campoTipo.value === "MOTORIZADA") {
    if (!campoPotencia.value || Number(campoPotencia.value) <= 0) {
      marcarErro(campoPotencia, "Informe a potência em cv (maior que zero).");
    }
  } else if (campoTipoImplemento.value.trim() === "") {
    marcarErro(campoTipoImplemento, "Informe o tipo do implemento.");
  }

  focarPrimeiroErro(formulario);
  return !formulario.querySelector(".invalido");
}

formulario.addEventListener("submit", async function (evento) {
  evento.preventDefault();
  limparMensagem();
  if (!validarFormulario()) {
    mostrarMensagem("Corrija os campos destacados.", "erro");
    return;
  }

  const motorizada = campoTipo.value === "MOTORIZADA";
  const dados = {
    tipo: campoTipo.value,
    nome: campoNome.value.trim(),
    descricao: campoDescricao.value.trim(),
    valorDiaria: campoValor.value === "" || Number(campoValor.value) === 0 ? null : Number(campoValor.value),
    disponivel: campoDisponivel.value === "true",
    potenciaCv: motorizada ? Number(campoPotencia.value) : null,
    tipoCombustivel: motorizada ? campoCombustivel.value : null,
    tipoImplemento: motorizada ? null : campoTipoImplemento.value.trim(),
    compatibilidade: motorizada ? null : campoCompatibilidade.value.trim()
  };

  botaoSalvar.disabled = true;
  try {
    if (campoId.value) {
      await apiPut("/equipamentos/" + campoId.value, dados);
      mostrarMensagem("Equipamento \"" + dados.nome + "\" atualizado.", "sucesso");
    } else {
      await apiPost("/equipamentos", dados);
      mostrarMensagem("Equipamento \"" + dados.nome + "\" cadastrado! Ele já aparece no catálogo.", "sucesso");
    }
    sairDaEdicao();
    await carregarMeusEquipamentos();
  } catch (erro) {
    mostrarMensagem(erro.message, "erro");
  } finally {
    botaoSalvar.disabled = false;
  }
});

function editarEquipamento(id) {
  const e = meusEquipamentos.find(function (item) { return item.id === id; });
  if (!e) {
    return;
  }
  limparErros(formulario);
  limparMensagem();
  campoId.value = e.id;
  campoTipo.value = e.tipo;
  campoTipo.disabled = true; // o tipo não muda depois do cadastro
  campoNome.value = e.nome;
  campoDescricao.value = e.descricao || "";
  campoValor.value = e.valorDiaria ? e.valorDiaria : "";
  campoDisponivel.value = String(e.disponivel);
  campoPotencia.value = e.potenciaCv || "";
  campoCombustivel.value = e.tipoCombustivel || "DIESEL";
  campoTipoImplemento.value = e.tipoImplemento || "";
  campoCompatibilidade.value = e.compatibilidade || "";
  alternarCamposDoTipo();
  tituloForm.textContent = "Editando: " + e.nome;
  botaoSalvar.textContent = "Salvar alterações";
  botaoCancelar.hidden = false;
  mostrarFormulario();
  campoNome.focus({ preventScroll: true });
}

function mostrarFormulario() {
  const coluna = document.querySelector(".coluna-formulario");
  if (window.matchMedia("(min-width: 901px)").matches) {
    coluna.scrollTop = 0;
  } else {
    coluna.scrollIntoView({ behavior: "smooth", block: "start" });
  }
}

function sairDaEdicao() {
  formulario.reset();
  campoId.value = "";
  campoTipo.disabled = false;
  limparErros(formulario);
  alternarCamposDoTipo();
  tituloForm.textContent = "Cadastrar equipamento";
  botaoSalvar.textContent = "Cadastrar equipamento";
  botaoCancelar.hidden = true;
}

async function removerEquipamento(id) {
  const e = meusEquipamentos.find(function (item) { return item.id === id; });
  if (!e || !confirm("Remover \"" + e.nome + "\"? Se ele já teve reservas, o histórico continua guardado.")) {
    return;
  }
  limparMensagem();
  try {
    const resultado = await apiDelete("/equipamentos/" + id);
    mostrarMensagem(resultado && resultado.apagado
      ? "Equipamento \"" + e.nome + "\" excluído."
      : "Equipamento \"" + e.nome + "\" removido do catálogo. O histórico de reservas dele continua guardado.", "sucesso");
    sairDaEdicao();
    await carregarMeusEquipamentos();
  } catch (erro) {
    mostrarMensagem(erro.message, "erro");
  }
}

campoTipo.addEventListener("change", alternarCamposDoTipo);
botaoCancelar.addEventListener("click", sairDaEdicao);

if (exigirLogin()) {
  alternarCamposDoTipo();
  if (new URLSearchParams(window.location.search).has("bemvindo")) {
    mostrarMensagem("Conta criada! Se tiver máquinas ou implementos parados, cadastre-os aqui. " +
      "Se só quiser pegar emprestado, vá ao Catálogo.", "sucesso");
  }
  carregarMeusEquipamentos();
}
