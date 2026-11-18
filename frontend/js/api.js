const SERVIDOR_API = window.location.protocol === "file:" ? "localhost" : window.location.hostname;
const API_BASE = "http://" + SERVIDOR_API + ":8080/api";

function lerArmazenamento(chave) {
  try {
    return localStorage.getItem(chave);
  } catch (erro) {
    return null;
  }
}

function getSessao() {
  const token = lerArmazenamento("token");
  if (!token) {
    return null;
  }
  return {
    token: token,
    id: Number(lerArmazenamento("produtorId")),
    nome: lerArmazenamento("produtorNome")
  };
}

function salvarSessao(respostaLogin) {
  try {
    localStorage.setItem("token", respostaLogin.token);
    localStorage.setItem("produtorId", respostaLogin.produtor.id);
    localStorage.setItem("produtorNome", respostaLogin.produtor.nome);
  } catch (erro) {
  }
}

function atualizarNomeNaSessao(nome) {
  try {
    localStorage.setItem("produtorNome", nome);
  } catch (erro) {
  }
}

function limparSessao() {
  document.documentElement.classList.remove("logado");
  try {
    localStorage.removeItem("token");
    localStorage.removeItem("produtorId");
    localStorage.removeItem("produtorNome");
  } catch (erro) {
  }
}

function irParaLogin() {
  const paginaAtual = window.location.pathname.split("/").pop() || "index.html";
  window.location.href = "entrar.html?voltar=" + encodeURIComponent(paginaAtual);
}

function exigirLogin() {
  if (!getSessao()) {
    irParaLogin();
    return false;
  }
  return true;
}

async function sair() {
  try {
    await apiPost("/auth/logout");
  } catch (erro) {
  }
  limparSessao();
  window.location.href = "index.html";
}

function montarAreaUsuario() {
  const area = document.getElementById("area-usuario");
  if (!area) {
    return;
  }
  const sessao = getSessao();
  const pagina = window.location.pathname.split("/").pop() || "index.html";
  const atual = function (arquivo) {
    return pagina === arquivo ? ' aria-current="page"' : "";
  };

  if (!sessao) {
    area.innerHTML =
      '<a class="link-criar-conta" href="perfil.html"' + atual("perfil.html") + ">Criar conta</a>" +
      '<a class="botao-entrar" href="entrar.html"' + atual("entrar.html") + ">Entrar</a>";
    montarContaVisitanteNoMenuMobile(atual);
    return;
  }

  const primeiroNome = (sessao.nome || "").split(" ")[0];
  const noPerfil = pagina === "perfil.html";
  area.innerHTML =
    '<div class="menu-usuario">' +
      '<button type="button" class="menu-usuario-botao' + (noPerfil ? " ativo" : "") + '" id="menu-usuario-botao" ' +
        'aria-expanded="false" aria-controls="menu-usuario-painel">' +
        '<span class="saudacao">Olá, </span><strong>' + escaparHtml(primeiroNome) + "</strong>" +
        '<span class="seta" aria-hidden="true">▾</span>' +
        (noPerfil ? '<span class="visualmente-oculto"> (você está no seu perfil)</span>' : "") +
      "</button>" +
      '<div class="menu-usuario-painel" id="menu-usuario-painel" hidden>' +
        '<a href="perfil.html"' + atual("perfil.html") + ">Meu perfil</a>" +
        '<hr class="menu-usuario-divisoria">' +
        '<button type="button" class="menu-usuario-sair" id="botao-sair">Sair</button>' +
      "</div>" +
    "</div>";

  document.getElementById("botao-sair").addEventListener("click", sair);
  iniciarMenuUsuario();
  montarContaNoMenuMobile(primeiroNome, atual("perfil.html"));
}

function montarContaVisitanteNoMenuMobile(atual) {
  const menu = document.getElementById("menu-principal");
  if (!menu) {
    return;
  }
  menu.querySelectorAll(".menu-conta, .menu-conectado").forEach(function (el) { el.remove(); });
  const conta = document.createElement("div");
  conta.className = "menu-conta";
  conta.innerHTML =
    '<ul><li><a href="entrar.html"' + atual("entrar.html") + ">Entrar</a></li>" +
    '<li><a href="perfil.html"' + atual("perfil.html") + ">Criar conta</a></li></ul>";
  menu.appendChild(conta);
}

function montarContaNoMenuMobile(primeiroNome, atributoAtual) {
  const menu = document.getElementById("menu-principal");
  if (!menu) {
    return;
  }
  menu.querySelectorAll(".menu-conta, .menu-conectado").forEach(function (el) { el.remove(); });

  const conectado = document.createElement("p");
  conectado.className = "menu-conectado";
  conectado.innerHTML = "Conectado como <strong>" + escaparHtml(primeiroNome) + "</strong>";
  menu.insertBefore(conectado, menu.firstChild);

  const conta = document.createElement("div");
  conta.className = "menu-conta";
  conta.innerHTML =
    '<ul><li><a href="perfil.html"' + atributoAtual + ">Meu perfil</a></li></ul>" +
    '<button type="button" class="menu-conta-sair">Sair</button>';
  menu.appendChild(conta);
  conta.querySelector(".menu-conta-sair").addEventListener("click", sair);
}

function iniciarMenuUsuario() {
  const caixa = document.querySelector(".menu-usuario");
  const botao = document.getElementById("menu-usuario-botao");
  const painel = document.getElementById("menu-usuario-painel");

  function abrir() {
    painel.hidden = false;
    botao.setAttribute("aria-expanded", "true");
  }

  function fechar(devolverFoco) {
    if (painel.hidden) {
      return;
    }
    painel.hidden = true;
    botao.setAttribute("aria-expanded", "false");
    if (devolverFoco) {
      botao.focus();
    }
  }

  botao.addEventListener("click", function () {
    if (painel.hidden) {
      abrir();
    } else {
      fechar(false);
    }
  });

  document.addEventListener("click", function (evento) {
    if (!caixa.contains(evento.target)) {
      fechar(false);
    }
  });

  caixa.addEventListener("keydown", function (evento) {
    if (evento.key === "Escape") {
      fechar(true);
    }
  });

  caixa.addEventListener("focusout", function (evento) {
    if (evento.relatedTarget && !caixa.contains(evento.relatedTarget)) {
      fechar(false);
    }
  });
}

// semRedirecionar = true: se o login tiver vencido, só limpa a sessão em vez de mandar para a tela de login.
// Usado em páginas públicas (ex.: catálogo), que continuam funcionando para visitantes.
async function chamarApi(metodo, caminho, corpo, semRedirecionar) {
  const sessao = getSessao();
  const opcoes = { method: metodo, headers: {} };
  if (sessao) {
    opcoes.headers["Authorization"] = "Bearer " + sessao.token;
  }
  if (corpo !== undefined) {
    opcoes.headers["Content-Type"] = "application/json";
    opcoes.body = JSON.stringify(corpo);
  }

  let resposta;
  try {
    resposta = await fetch(API_BASE + caminho, opcoes);
  } catch (erro) {
    throw new Error("Não foi possível conectar à API em " + API_BASE +
      ". Verifique se o backend está rodando.");
  }

  if (resposta.status === 401 && sessao) {
    limparSessao();
    if (semRedirecionar) {
      montarAreaUsuario();
    } else {
      irParaLogin();
    }
    throw new Error("Sua sessão expirou. Entre novamente.");
  }

  if (resposta.status === 204) {
    return null;
  }

  let dados = null;
  try {
    dados = await resposta.json();
  } catch (erro) {
    dados = null;
  }

  if (!resposta.ok) {
    throw new Error((dados && dados.erro) || "Erro " + resposta.status + " ao falar com o servidor.");
  }
  return dados;
}

function apiGet(caminho, semRedirecionar) {
  return chamarApi("GET", caminho, undefined, semRedirecionar);
}

function periodosSobrepostos(inicioA, fimA, inicioB, fimB) {
  return !(fimA < inicioB || inicioA > fimB);
}

function apiPost(caminho, corpo) {
  return chamarApi("POST", caminho, corpo);
}

function apiPut(caminho, corpo) {
  return chamarApi("PUT", caminho, corpo);
}

function apiDelete(caminho) {
  return chamarApi("DELETE", caminho);
}

function iniciarMenu() {
  const botao = document.querySelector(".botao-menu");
  const menu = document.getElementById("menu-principal");
  if (!botao || !menu) {
    return;
  }
  function fecharMenu() {
    menu.classList.remove("aberto");
    botao.setAttribute("aria-expanded", "false");
  }

  botao.addEventListener("click", function () {
    const aberto = menu.classList.toggle("aberto");
    botao.setAttribute("aria-expanded", aberto ? "true" : "false");
  });

  menu.addEventListener("keydown", function (evento) {
    if (evento.key === "Escape" && menu.classList.contains("aberto")) {
      fecharMenu();
      botao.focus();
    }
  });

  // O modo "☰ Menu" vale só até 768px (mesmo limite do CSS).
  // Se a tela passar disso com o painel aberto (ex.: girar o tablet), o painel é fechado.
  const telaLarga = window.matchMedia("(min-width: 769px)");
  const aoMudarLargura = function () {
    if (telaLarga.matches) {
      fecharMenu();
    }
  };
  if (telaLarga.addEventListener) {
    telaLarga.addEventListener("change", aoMudarLargura);
  } else {
    telaLarga.addListener(aoMudarLargura); // navegadores antigos
  }
}

function mostrarMensagem(texto, tipo, idElemento) {
  const caixa = document.getElementById(idElemento || "mensagem");
  if (!caixa) {
    return;
  }
  caixa.textContent = texto;
  caixa.className = "mensagem " + tipo;
  caixa.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function limparMensagem(idElemento) {
  const caixa = document.getElementById(idElemento || "mensagem");
  if (caixa) {
    caixa.textContent = "";
    caixa.className = "mensagem";
  }
}

function marcarErro(campo, texto) {
  campo.classList.add("invalido");
  campo.setAttribute("aria-invalid", "true");
  const erro = document.getElementById("erro-" + campo.id);
  if (erro) {
    erro.textContent = texto;
  }
}

function limparErros(formulario) {
  formulario.querySelectorAll(".invalido").forEach(function (campo) {
    campo.classList.remove("invalido");
    campo.removeAttribute("aria-invalid");
  });
  formulario.querySelectorAll(".erro-campo").forEach(function (erro) {
    erro.textContent = "";
  });
}

function focarPrimeiroErro(formulario) {
  const primeiro = formulario.querySelector(".invalido");
  if (primeiro) {
    primeiro.focus();
  }
}

// As mesmas regras do backend (classe Validacoes.java): aqui servem para avisar na hora,
// o servidor confere de novo.

const REGEX = {
  digitosRepetidos: /^(\d)\1+$/,
  // DDD 11-99 + fixo (8 dígitos começando de 2 a 5) ou celular (9 dígitos começando com 9)
  telefone: /^[1-9][1-9](?:[2-5]\d{7}|9\d{8})$/,
  nome: /^(?=.*\p{L})[\p{L}\d .'&ºª-]{3,100}$/u,
  municipio: /^\p{L}[\p{L} '-]{1,79}$/u,
  senha: /^(?=.*\p{L})(?=.*\d).{6,72}$/u,
  temLetra: /\p{L}/u
};

function somenteDigitos(texto) {
  return String(texto || "").replace(/\D/g, "");
}

function cpfValido(cpf) {
  if (!/^\d{11}$/.test(cpf) || REGEX.digitosRepetidos.test(cpf)) {
    return false;
  }
  for (let posicao = 9; posicao <= 10; posicao++) {
    let soma = 0;
    for (let i = 0; i < posicao; i++) {
      soma += Number(cpf[i]) * (posicao + 1 - i);
    }
    const digito = (soma * 10) % 11 % 10;
    if (digito !== Number(cpf[posicao])) {
      return false;
    }
  }
  return true;
}

function cnpjValido(cnpj) {
  if (!/^\d{14}$/.test(cnpj) || REGEX.digitosRepetidos.test(cnpj)) {
    return false;
  }
  const pesos1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  const pesos2 = [6].concat(pesos1);
  return [pesos1, pesos2].every(function (pesos) {
    const soma = pesos.reduce(function (total, peso, i) { return total + peso * Number(cnpj[i]); }, 0);
    const resto = soma % 11;
    const digito = resto < 2 ? 0 : 11 - resto;
    return digito === Number(cnpj[pesos.length]);
  });
}

function erroDocumento(texto) {
  const digitos = somenteDigitos(texto);
  if (digitos.length !== 11 && digitos.length !== 14) {
    return "Digite um CPF (11 números) ou CNPJ (14 números).";
  }
  if (digitos.length === 11 && !cpfValido(digitos)) {
    return "CPF inválido. Confira os números.";
  }
  if (digitos.length === 14 && !cnpjValido(digitos)) {
    return "CNPJ inválido. Confira os números.";
  }
  return "";
}

function formatarDocumento(texto) {
  const d = somenteDigitos(texto).slice(0, 14);
  if (d.length <= 11) {
    return d
      .replace(/^(\d{3})(\d)/, "$1.$2")
      .replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
      .replace(/\.(\d{3})(\d{1,2})$/, ".$1-$2");
  }
  return d
    .replace(/^(\d{2})(\d)/, "$1.$2")
    .replace(/^(\d{2})\.(\d{3})(\d)/, "$1.$2.$3")
    .replace(/\.(\d{3})(\d)/, ".$1/$2")
    .replace(/(\d{4})(\d{1,2})$/, "$1-$2");
}

function formatarTelefoneDigitado(texto) {
  const d = somenteDigitos(texto).slice(0, 11);
  if (d.length <= 2) {
    return d.length ? "(" + d : "";
  }
  const ddd = "(" + d.slice(0, 2) + ") ";
  const resto = d.slice(2);
  if (resto.length <= 4) {
    return ddd + resto;
  }
  const meio = resto.length === 9 ? 5 : 4;
  return ddd + resto.slice(0, meio) + "-" + resto.slice(meio);
}

function aplicarMascara(campo, formatar) {
  if (!campo) {
    return;
  }
  campo.addEventListener("input", function () {
    campo.value = formatar(campo.value);
  });
  if (campo.value) {
    campo.value = formatar(campo.value);
  }
}

// Fecha um <dialog> pelos botões com data-fechar e pelo clique no fundo escurecido.
// (O conteúdo fica num <div class="modal-conteudo">, então só o fundo "acerta" o próprio <dialog>.)
function ligarFechamentoModal(dialogo) {
  dialogo.querySelectorAll("[data-fechar]").forEach(function (botao) {
    botao.addEventListener("click", function () { dialogo.close(); });
  });
  dialogo.addEventListener("click", function (evento) {
    if (evento.target === dialogo) {
      dialogo.close();
    }
  });
}

function criarModal(id, html) {
  let dialogo = document.getElementById(id);
  if (dialogo) {
    return dialogo;
  }
  dialogo = document.createElement("dialog");
  dialogo.id = id;
  dialogo.className = "modal";
  dialogo.setAttribute("aria-labelledby", id + "-titulo");
  dialogo.innerHTML = '<div class="modal-conteudo">' + html + "</div>";
  document.body.appendChild(dialogo);
  ligarFechamentoModal(dialogo);
  return dialogo;
}

async function abrirAvaliacoes(produtorId, nome, media, total) {
  const dialogo = criarModal("modal-avaliacoes",
    '<button type="button" class="modal-fechar" data-fechar aria-label="Fechar">✕</button>' +
    '<h2 id="modal-avaliacoes-titulo"></h2>' +
    '<p class="modal-avaliacoes-resumo" id="modal-avaliacoes-resumo"></p>' +
    '<div id="modal-avaliacoes-lista"></div>' +
    '<div class="modal-acoes"><button type="button" class="botao botao-secundario" data-fechar>Fechar</button></div>');

  document.getElementById("modal-avaliacoes-titulo").textContent = "Avaliações de " + nome;
  document.getElementById("modal-avaliacoes-resumo").innerHTML =
    '<span class="estrelas">' + icone("star", "icone-preenchido") + Number(media).toFixed(1).replace(".", ",") +
    '<span class="visualmente-oculto"> de 5</span></span> de média em ' + total +
    (Number(total) === 1 ? " avaliação" : " avaliações");
  const lista = document.getElementById("modal-avaliacoes-lista");
  lista.innerHTML = '<p class="dica">Carregando avaliações...</p>';

  dialogo.showModal();

  try {
    const avaliacoes = await apiGet("/produtores/" + produtorId + "/avaliacoes", true);
    lista.innerHTML = listaDeAvaliacoes(avaliacoes);
  } catch (erro) {
    lista.innerHTML = '<p class="dica">' + escaparHtml(erro.message) + "</p>";
  }
}

document.addEventListener("click", function (evento) {
  const botao = evento.target.closest(".ver-avaliacoes");
  if (botao) {
    abrirAvaliacoes(botao.dataset.produtorId, botao.dataset.nome, botao.dataset.media, botao.dataset.total);
  }
});

function listaDeAvaliacoes(avaliacoes) {
  if (avaliacoes.length === 0) {
    return '<p class="dica">Ainda não recebeu avaliações.</p>';
  }
  return '<ul class="lista-avaliacoes">' + avaliacoes.map(function (a) {
    return (
      "<li>" +
        estrelasDaNota(a.nota) + " " +
        "<strong>" + escaparHtml(a.avaliadorNome) + "</strong> · " + formatarData(a.data) +
        '<br><span class="dica">' + escaparHtml(a.equipamentoNome) + "</span>" +
        (a.comentario ? "<p>" + escaparHtml(a.comentario) + "</p>" : "") +
      "</li>"
    );
  }).join("") + "</ul>";
}

function blocoReputacao(produtorId, media, total, nome) {
  if (!total) {
    return '<span class="dica">Ainda sem avaliações</span>';
  }
  return (
    '<span class="reputacao">' +
      '<span class="estrelas">' + icone("star", "icone-preenchido") +
        Number(media).toFixed(1).replace(".", ",") +
        '<span class="visualmente-oculto"> de 5</span></span>' +
      '<button type="button" class="link-botao ver-avaliacoes" aria-haspopup="dialog" ' +
        'data-produtor-id="' + produtorId + '" data-nome="' + escaparHtml(nome) + '" ' +
        'data-media="' + media + '" data-total="' + total + '" ' +
        'aria-label="Ver ' + total + (total === 1 ? " avaliação" : " avaliações") + " de " + escaparHtml(nome) + '">' +
        total + (total === 1 ? " avaliação" : " avaliações") +
      "</button>" +
    "</span>"
  );
}

function estrelasDaNota(nota) {
  let html = '<span class="estrelas"><span class="visualmente-oculto">Nota ' + nota + " de 5</span>";
  for (let i = 1; i <= 5; i++) {
    html += icone("star", i <= nota ? "icone-preenchido" : "estrelas-vazias");
  }
  return html + "</span>";
}

function icone(nome, classeExtra) {
  return '<span class="material-symbols-rounded' + (classeExtra ? " " + classeExtra : "") +
    '" aria-hidden="true">' + nome + "</span>";
}

function pillStatus(status) {
  return '<span class="etiqueta pill status-' + status + '">' + NOMES_STATUS[status] + "</span>";
}

function pillPausado() {
  return '<span class="etiqueta pill pill-pausado">Pausado</span>';
}

function fichaTecnica(tipo, descricaoTecnica) {
  const nomeTipo = tipo === "MOTORIZADA" ? "Máquina motorizada" : "Implemento";
  return '<p class="ficha">' + nomeTipo + " · " + escaparHtml(descricaoTecnica) + "</p>";
}

function blocoPreco(valor, sufixo) {
  if (!valor || Number(valor) === 0) {
    return '<p class="preco preco-gratuito">Empréstimo gratuito</p>';
  }
  return '<p class="preco">' + formatarMoeda(valor) + (sufixo ? " <small>" + sufixo + "</small>" : "") + "</p>";
}

function formatarMoeda(valor) {
  if (valor === null || valor === undefined) {
    return "-";
  }
  return Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatarData(textoIso) {
  if (!textoIso) {
    return "-";
  }
  const partes = String(textoIso).substring(0, 10).split("-");
  return partes[2] + "/" + partes[1] + "/" + partes[0];
}

function formatarTelefone(numeros) {
  return numeros ? formatarTelefoneDigitado(numeros) : "";
}

function hojeIso() {
  const hoje = new Date();
  const mes = String(hoje.getMonth() + 1).padStart(2, "0");
  const dia = String(hoje.getDate()).padStart(2, "0");
  return hoje.getFullYear() + "-" + mes + "-" + dia;
}

// Evita que textos digitados pelos usuários virem HTML na página.
function escaparHtml(texto) {
  if (texto === null || texto === undefined) {
    return "";
  }
  return String(texto)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

const NOMES_STATUS = {
  PENDENTE: "Pendente",
  APROVADA: "Aprovada",
  REJEITADA: "Rejeitada",
  CANCELADA: "Cancelada",
  CONCLUIDA: "Devolvida"
};

iniciarMenu();
montarAreaUsuario();
