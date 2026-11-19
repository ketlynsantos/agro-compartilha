# AgroCompartilha

**Global Solution 2026/2 · FIAP · 2º ano de Engenharia de Software (2ESOA) · Tema: CitizenTech**

Plataforma em que **pequenos produtores rurais compartilham máquinas e implementos agrícolas** que ficam parados boa parte do ano. Quem tem um trator, uma grade ou uma plantadeira oferece aos vizinhos (de graça ou com diária); quem precisa pede as datas, o dono aprova e, na devolução, um avalia o outro.

---

## 1. O problema e o cidadão beneficiado

- **Quem:** o pequeno produtor rural e a agricultura familiar, que não consegue comprar máquinas caras.
- **Problema:** um trator novo custa mais do que muitos pequenos produtores faturam em anos, enquanto as máquinas da região ficam paradas fora da época de uso.
- **Solução:** um catálogo regional de equipamentos com pedido de reserva, aprovação pelo dono, valor calculado na hora e **reputação** (avaliação mútua) para gerar confiança entre vizinhos.

**ODS da ONU atendidos**

| ODS | Como o projeto contribui |
|---|---|
| **2 – Fome zero e agricultura sustentável** | Dá ao pequeno produtor acesso a máquinas que aumentam a produtividade da lavoura. |
| **10 – Redução das desigualdades** | Quem não pode comprar passa a ter acesso, por diária baixa ou empréstimo gratuito. |
| **12 – Consumo e produção responsáveis** | Aproveita melhor equipamentos que já existem, em vez de cada um comprar o seu. |

---

## 2. Estrutura da entrega

```
agrocompartilha/
├── README.md                    ← este arquivo
├── backend/                     ← Java + JDBC + API REST (Maven)
│   ├── pom.xml
│   ├── .env.example             ← modelo das credenciais do Oracle
│   ├── db/schema.sql            ← script do banco (CREATE TABLE + INSERTs)
│   └── src/main/java/br/com/fiap/agrocompartilha/
│       ├── model/               ← entidades (POO, herança)
│       ├── dao/                 ← acesso ao banco com JDBC (CRUD)
│       ├── service/             ← regras de negócio e validações
│       ├── resource/            ← API REST (JAX-RS) e conversão para DTO
│       ├── resource/mapper/     ← tratamento de erros (exceção → resposta JSON)
│       ├── dto/  exception/  factory/  seguranca/
│       └── Main.java            ← sobe o servidor
├── frontend/                    ← HTML + CSS + JavaScript puros
│   ├── index.html               ← catálogo (página inicial)
│   ├── entrar.html  perfil.html  cadastro-equipamento.html
│   ├── minhas-reservas.html  reservas-recebidas.html
│   ├── css/style.css
│   ├── js/                      ← api.js (compartilhado) + um script por página
│   └── img/logotipo.png
└── docs/diagrams/
    ├── diagrama-classes.puml    ← diagrama de classes (UML)
    ├── diagrama-classes.png
    ├── diagrama-er.puml         ← diagrama entidade-relacionamento
    └── diagrama-er.png
```

---

## 3. Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17+ (compilado com `release 17`) |
| Build | Maven |
| Banco | Oracle (servidor da FIAP), acessado com **JDBC** (`ojdbc11`) e `PreparedStatement` |
| API | JAX-RS (Jersey 3.1) com servidor Grizzly embutido, JSON-B |
| Senhas | PBKDF2-SHA256 com *salt* (do próprio JDK: `javax.crypto`) |
| Web | HTML5 semântico, CSS3 e JavaScript puro (sem framework), fonte Inter e ícones Material Symbols (Google Fonts) |

---

## 4. Como executar

### Pré-requisitos
- **JDK 17 ou mais novo** e **Maven** (ou o IntelliJ IDEA, que já traz o Maven).
- Acesso ao **Oracle da FIAP** (usuário `rmXXXXXX` e senha).
- Um navegador (Chrome, Edge ou Firefox).

### Passo 1 – Criar o banco
1. Abra o **SQL Developer** conectado ao Oracle da FIAP.
2. Abra o arquivo `backend/db/schema.sql` e execute **o script inteiro** (F5).
3. O script apaga e recria as 6 tabelas e insere os dados de exemplo. Pode ser executado de novo quantas vezes quiser.

### Passo 2 – Configurar as credenciais do Oracle
Dentro de `backend/`, copie o arquivo `.env.example` para **`.env`** e preencha:

```
ORACLE_JDBC_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
ORACLE_USER=rmXXXXXX
ORACLE_PASSWORD=sua_senha
```

### Passo 3 – Subir o backend (API)
**Pelo terminal**, dentro de `backend/`:

```bash
mvn clean compile exec:java
```

**Pelo IntelliJ:** abra a pasta `backend` como projeto Maven e execute a classe `br.com.fiap.agrocompartilha.Main`.
Na configuração de execução, o **Working directory** deve ser a pasta `backend` (é onde está o `.env`).

Quando aparecer `API do AgroCompartilha rodando!`, teste no navegador:
`http://localhost:8080/api/equipamentos` → deve mostrar a lista de equipamentos em JSON.

> Porta ocupada? Use outra: `mvn exec:java -Dporta=8081` (e ajuste `API_BASE` em `frontend/js/api.js`).

### Passo 4 – Abrir o site
Com a API rodando, abra **`frontend/index.html`** direto no navegador (duplo clique).
Para testar no celular na mesma rede Wi-Fi, abra a pasta `frontend` com um servidor local (ex.: extensão *Live Server* do VS Code) e acesse pelo IP do computador.

### Usuários de teste (carga inicial do `schema.sql`)

| Usuário | CPF / CNPJ | Senha |
|---|---|---|
| José Aparecido da Silva | 111.222.333-96 | agro123 |
| Maria das Graças Oliveira | 555.666.777-20 | agro123 |
| Antônio Carlos Pereira | 999.888.777-14 | agro123 |
| Cooperativa Vale Verde | 12.345.678/0001-95 | agro123 |

**Roteiro rápido de teste:** entre como **Maria**, peça a *Plantadeira* no catálogo → saia e entre como **Antônio** (dono) → em *Reservas recebidas*, veja a reputação da Maria e aprove → o telefone de cada um aparece para combinarem a retirada.

---

## 5. Funcionalidades

| Página | O que faz |
|---|---|
| **Catálogo** (`index.html`) | Lista pública dos equipamentos com busca e filtros; pedido de reserva em modal com cálculo do valor e aviso de datas já reservadas. |
| **Entrar / Criar conta** | Login com CPF/CNPJ e senha; cadastro com validação de CPF/CNPJ (dígitos verificadores), telefone e senha. |
| **Meu perfil** | Editar dados e senha, ver as avaliações recebidas, excluir a conta. |
| **Meus equipamentos** | Cadastrar, listar, editar e remover máquinas e implementos. |
| **Minhas reservas** | Acompanhar os pedidos feitos, cancelar, avaliar o dono após a devolução. |
| **Reservas recebidas** | Ver a reputação de quem pediu, aprovar ou rejeitar, marcar como devolvido, avaliar quem pegou. |

### Regras de negócio principais
- O **valor é fixado no pedido** (diária × dias); empréstimo gratuito não tem custo.
- Pedidos **pendentes podem se sobrepor**; o conflito de datas é barrado **na aprovação** e também ao pedir datas que **já estão aprovadas** para outra pessoa.
- Só o **dono** aprova, rejeita ou marca como devolvido; só **quem pediu** cancela (e apenas antes do início).
- Máquinas saem e voltam com o **mesmo nível de combustível**.
- **Avaliação mútua** só depois da devolução, uma vez por pessoa; nota 1 ou 2 exige comentário.
- **Privacidade:** o CPF nunca aparece para outros usuários e o telefone só é mostrado depois que a reserva é aprovada.

---

## 6. Arquitetura e requisitos da atividade

### Java + JDBC
- **POO:** `Equipamento` é **abstrata** e tem duas subclasses (`MaquinaMotorizada` e `ImplementoAgricola`) com **polimorfismo** em `getTipo()` e `getDescricaoTecnica()`. Entidades relacionadas: `Produtor`, `Equipamento`, `Reserva`, `Avaliacao`, `Reputacao`. Atributos privados com getters/setters.
- **Camadas separadas:** `model` → `dao` (JDBC) → `service` (regras) → `resource` (interface REST), cada uma no seu pacote.
- **CRUD via JDBC:**

| Entidade | Inserir | Listar | Buscar | Atualizar | Excluir |
|---|---|---|---|---|---|
| Produtor | `inserir` | `listarTodos` | `buscarPorId`, `buscarPorDocumento` | `atualizar`, `atualizarSenha` | `excluir` |
| Equipamento | `inserir` | `listarAtivos`, `listarAtivosPorDono` | `buscarPorId` | `atualizar` | `excluir` (DELETE) e `desativar` |
| Reserva | `inserir` | `listarPorSolicitante`, `listarPorDono`, `listarAprovadasPorEquipamento` | `buscarPorId` | `atualizarStatus` | — (ver abaixo) |
| Avaliação | `inserir` | `listarRecebidas` | `existeAvaliacao` | — | — |

> **Decisões de projeto:** um equipamento **sem reservas** é apagado de verdade (`DELETE`); um equipamento **com reservas** é desativado (`ativo = 0`), para não apagar o histórico de reservas e as avaliações dos produtores. Reservas nunca são apagadas, só mudam de status (`PENDENTE`, `APROVADA`, `REJEITADA`, `CANCELADA` e `CONCLUIDA`, que é quando o equipamento é devolvido).

- **Tratamento de erros:** exceções de banco e entradas inválidas viram mensagens claras em JSON (ex.: *"Já existe um cadastro com este CPF/CNPJ"*), sem derrubar o servidor (`resource/mapper`).

### Banco de dados
- 6 tabelas: `PRODUTOR`, `EQUIPAMENTO`, `MAQUINA_MOTORIZADA`, `IMPLEMENTO_AGRICOLA`, `RESERVA`, `AVALIACAO`.
- **N:N com tabela associativa:** `RESERVA` liga `PRODUTOR` (quem pede) a `EQUIPAMENTO`.
- Constraints: PK, FK, NOT NULL, UNIQUE (`documento`; `reserva_id + avaliador_id`) e CHECK (status, nota de 1 a 5, datas, valores).
- **Consultas com JOIN e agregação** usadas pelo sistema: a reputação de cada produtor é calculada com `AVG(nota)` e `COUNT(*)` agrupados por produtor (`ProdutorDAO`).

### Web
- 6 páginas com o **mesmo menu**, HTML semântico (`header`, `nav`, `main`, `section`, `footer`, `dialog`).
- Formulários com **validação em JavaScript** (regex de CPF/CNPJ, telefone, nome, senha) e mensagens na própria página.
- **Responsivo** de 320px a telas largas; contraste conferido no padrão WCAG AA.
- **A web lê e grava os dados reais do banco** pela API (diferencial).

### Diagramas
Em `docs/diagrams/`, escritos em PlantUML (dá para abrir em [plantuml.com](https://www.plantuml.com/plantuml) ou na extensão PlantUML do VS Code/IntelliJ):
- `diagrama-classes.puml`: diagrama de classes UML do domínio (pacote `model`): atributos, métodos de negócio, herança e relacionamentos com multiplicidade, conferidos com o código.
- `diagrama-er.puml`: diagrama entidade-relacionamento com tabelas, PK, FK e cardinalidades.

---

## 7. Endpoints da API (`http://localhost:8080/api`)

| Método | Rota | Login | Descrição |
|---|---|---|---|
| POST | `/auth/login` | — | Entrar (devolve o token) |
| POST | `/auth/logout` | ✔ | Sair |
| GET | `/auth/eu` | ✔ | Dados do produtor logado |
| POST | `/produtores` | — | Criar conta |
| GET | `/produtores` | — | Lista pública (nome, município, reputação) |
| GET | `/produtores/{id}` | — | Perfil público |
| GET | `/produtores/{id}/avaliacoes` | — | Avaliações recebidas |
| PUT / DELETE | `/produtores/eu` | ✔ | Editar / excluir a própria conta |
| GET | `/equipamentos` | — | Catálogo (filtro `?tipo=`) |
| GET | `/equipamentos/{id}` | — | Detalhe |
| GET | `/equipamentos/{id}/ocupacao` | — | Datas já reservadas (aprovadas) |
| GET | `/equipamentos/meus` | ✔ | Equipamentos do produtor logado |
| POST / PUT / DELETE | `/equipamentos` · `/equipamentos/{id}` | ✔ | Cadastrar / editar / remover (só o dono) |
| GET | `/reservas/minhas` · `/reservas/recebidas` · `/reservas/{id}` | ✔ | Pedidos feitos / recebidos / detalhe |
| POST | `/reservas` | ✔ | Pedir reserva |
| PUT | `/reservas/{id}/aprovar` · `rejeitar` · `concluir` · `cancelar` | ✔ | Mudar o status |
| POST | `/avaliacoes` | ✔ | Avaliar a outra parte de uma reserva devolvida |

O token vai no cabeçalho `Authorization: Bearer <token>`.

---

## 8. Uso de IA generativa

Usamos o **Claude (Anthropic)** como ferramenta de apoio nestas etapas:

| Para quê | Como ajudou |
|---|---|
| **Pesquisa da ideia** | Depois de escolhermos o ramo do agronegócio, pesquisamos com a IA qual seria a melhor ideia de CitizenTech para o pequeno produtor rural, o que levou ao compartilhamento de máquinas e implementos agrícolas. |
| **Construção do README.md** | Pedimos ajuda para organizar e escrever este README (estrutura, passo a passo de execução e explicação dos requisitos). |
| **Revisão de códigos com dificuldade** | Pedimos ajuda para revisar partes do código em que tivemos dificuldade, como o **tratamento de exceções no Java** (exceções próprias e a conversão dos erros em mensagens claras para o usuário). |

**Como usamos:** a IA foi usada como apoio. Revisamos e testamos o que foi sugerido antes de usar, e cada integrante sabe explicar a parte que apresenta.


