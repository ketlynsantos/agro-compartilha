const formulario = document.getElementById("form-login");
const campoDocumento = document.getElementById("documento");
const campoSenha = document.getElementById("senha");
const botaoEntrar = document.getElementById("botao-entrar");

function paginaDeRetorno() {
  const voltar = new URLSearchParams(window.location.search).get("voltar");
  return voltar && /^[a-z-]+\.html$/.test(voltar) && voltar !== "entrar.html" ? voltar : "index.html";
}

if (getSessao()) {
  window.location.href = paginaDeRetorno();
}

aplicarMascara(campoDocumento, formatarDocumento);

formulario.addEventListener("submit", async function (evento) {
  evento.preventDefault();
  limparErros(formulario);
  limparMensagem();

  const digitos = somenteDigitos(campoDocumento.value);
  const erroDoc = erroDocumento(campoDocumento.value);
  if (erroDoc) {
    marcarErro(campoDocumento, erroDoc);
  }
  if (!campoSenha.value) {
    marcarErro(campoSenha, "Digite a sua senha.");
  }
  if (formulario.querySelector(".invalido")) {
    focarPrimeiroErro(formulario);
    return;
  }

  botaoEntrar.disabled = true;
  try {
    const resposta = await apiPost("/auth/login", { documento: digitos, senha: campoSenha.value });
    salvarSessao(resposta);
    window.location.href = paginaDeRetorno();
  } catch (erro) {
    campoSenha.value = "";
    mostrarMensagem(erro.message, "erro");
    botaoEntrar.disabled = false;
  }
});
