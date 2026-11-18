const formulario = document.getElementById("form-perfil");
const campoNome = document.getElementById("nome");
const campoDocumento = document.getElementById("documento");
const campoTelefone = document.getElementById("telefone");
const campoMunicipio = document.getElementById("municipio");
const campoSenha = document.getElementById("senha");
const campoConfirmar = document.getElementById("confirmar-senha");
const botaoSalvar = document.getElementById("botao-salvar");

const sessaoAtual = getSessao();
const editando = sessaoAtual !== null;

async function iniciarPerfil() {
  if (!editando) {
    return; // modo cadastro: a página já vem pronta para isso
  }

  document.title = "AgroCompartilha | Meu perfil";
  document.getElementById("titulo-pagina").textContent = "Meu perfil";
  document.getElementById("subtitulo-pagina").textContent = "Mantenha seus dados atualizados para os vizinhos falarem com você.";
  document.getElementById("rotulo-senha").textContent = "Nova senha (opcional)";
  document.getElementById("dica-senha").textContent = "Deixe em branco para manter a senha atual.";
  document.getElementById("link-entrar").hidden = true;
  botaoSalvar.textContent = "Salvar alterações";

  try {
    const eu = await apiGet("/auth/eu");
    campoNome.value = eu.nome;
    campoDocumento.value = formatarDocumento(eu.documento);
    campoTelefone.value = formatarTelefone(eu.telefone);
    campoMunicipio.value = eu.municipio;
    mostrarReputacao(eu);
  } catch (erro) {
    mostrarMensagem(erro.message, "erro");
  }
}

async function mostrarReputacao(eu) {
  document.getElementById("secao-reputacao").hidden = false;
  document.getElementById("secao-excluir").hidden = false;
  const resumo = document.getElementById("resumo-reputacao");
  resumo.innerHTML = eu.totalAvaliacoes
    ? '<span class="estrelas">' + icone("star", "icone-preenchido") + eu.mediaAvaliacoes.toFixed(1).replace(".", ",") +
      "</span> de média em " + eu.totalAvaliacoes + (eu.totalAvaliacoes === 1 ? " avaliação" : " avaliações")
    : '<span class="dica">Você ainda não recebeu avaliações. Elas aparecem depois que um equipamento é devolvido.</span>';
  try {
    const avaliacoes = await apiGet("/produtores/" + eu.id + "/avaliacoes");
    document.getElementById("minhas-avaliacoes").innerHTML = avaliacoes.length ? listaDeAvaliacoes(avaliacoes) : "";
  } catch (erro) {
    mostrarMensagem(erro.message, "erro");
  }
}

function validarFormulario() {
  limparErros(formulario);

  if (!REGEX.nome.test(campoNome.value.trim())) {
    marcarErro(campoNome, "Informe o nome (pelo menos 3 caracteres; letras, números, espaço, ponto ou hífen).");
  }
  const erroDoc = erroDocumento(campoDocumento.value);
  if (erroDoc) {
    marcarErro(campoDocumento, erroDoc);
  }
  if (!REGEX.telefone.test(somenteDigitos(campoTelefone.value))) {
    marcarErro(campoTelefone, "Telefone inválido. Use DDD + número, ex.: (15) 99876-1234.");
  }
  if (!REGEX.municipio.test(campoMunicipio.value.trim())) {
    marcarErro(campoMunicipio, "Informe o município (só letras, espaço, hífen ou apóstrofo).");
  }

  const senhaObrigatoria = !editando;
  if (senhaObrigatoria || campoSenha.value) {
    if (campoSenha.value.length < 6) {
      marcarErro(campoSenha, "A senha precisa ter pelo menos 6 caracteres.");
    } else if (!REGEX.senha.test(campoSenha.value)) {
      marcarErro(campoSenha, "A senha precisa ter pelo menos uma letra e um número.");
    } else if (campoSenha.value !== campoConfirmar.value) {
      marcarErro(campoConfirmar, "As senhas não são iguais.");
    }
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

  const dados = {
    nome: campoNome.value.trim(),
    documento: campoDocumento.value.replace(/\D/g, ""),
    telefone: campoTelefone.value.replace(/\D/g, ""),
    municipio: campoMunicipio.value.trim(),
    senha: campoSenha.value
  };

  botaoSalvar.disabled = true;
  try {
    if (editando) {
      const atualizado = await apiPut("/produtores/eu", dados);
      atualizarNomeNaSessao(atualizado.nome);
      montarAreaUsuario();
      campoSenha.value = "";
      campoConfirmar.value = "";
      mostrarMensagem("Perfil atualizado" + (dados.senha ? " e senha trocada." : "."), "sucesso");
    } else {
      const resposta = await apiPost("/produtores", dados);
      salvarSessao(resposta);
      window.location.href = "cadastro-equipamento.html?bemvindo=1";
    }
  } catch (erro) {
    mostrarMensagem(erro.message, "erro");
  } finally {
    botaoSalvar.disabled = false;
  }
});

document.getElementById("botao-excluir").addEventListener("click", async function () {
  if (!confirm("Tem certeza que deseja excluir sua conta? Essa ação não pode ser desfeita.")) {
    return;
  }
  limparMensagem();
  try {
    await apiDelete("/produtores/eu");
    limparSessao();
    window.location.href = "index.html";
  } catch (erro) {
    mostrarMensagem(erro.message, "erro");
  }
});

aplicarMascara(campoDocumento, formatarDocumento);
aplicarMascara(campoTelefone, formatarTelefoneDigitado);

iniciarPerfil();
