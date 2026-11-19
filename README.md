# ResolveAI API

API RESTful do **ResolveAI**, plataforma de ocorrências e transparência urbana desenvolvida pela **HalfTech** para a Global Solution 2026/2 da FIAP (tema CitizenTech).

O morador registra um problema da rua (buraco, calçada quebrada, iluminação apagada), os vizinhos apoiam com "Eu também vi", todos acompanham o status e o histórico, e o placar compara quanto cada bairro resolve.

| | |
|---|---|
| **API no Render** | https://resolveai-api-3tvg.onrender.com|
| **Vídeo (YouTube)** | [PREENCHER: link do vídeo não listado] |
| **Front-end** | https://github.com/ResolveAI-Global-Solution/Front-end-ResolveAi |
| **Health check** | `GET /health` → `{"status":"ok"}` |

## Integrantes · Turma 1TDSPW

| Nome | RM |
|---|---|
| Andrei Oliveira de Sousa | 573373 |
| Felipe Coelho da Fonseca | 572133 |
| Henrique Soares Pereira | 569898 |
| Luiz Felipe Lourenço da Silva | 572576 |

## Tecnologias

- Java 21 e Spring Boot 3.3 (Web e Validation)
- JDBC puro (`DataSource` + `PreparedStatement`), sem JPA, Hibernate ou Spring Data
- Oracle Database (Oracle FIAP), driver `ojdbc11`
- ModelMapper para a conversão entre model e DTO
- Bean Validation nos DTOs de entrada
- BCrypt (`spring-security-crypto`) para guardar a senha com hash
- Maven, Docker (build multi-stage) e Render

## Estrutura do projeto

```
src/main/java/br/com/fiap/resolveai
├── model       entidades do domínio e enums (Ocorrencia, HistoricoStatus, Apoio, Usuario, Categoria...)
├── dao         acesso ao Oracle com JDBC, um DAO por entidade, DataSource recebido pelo construtor
├── dto         DTOs de entrada (com validação) e de saída
├── service     regras de negócio e orquestração dos DAOs
├── controller  rotas REST e status HTTP
├── exception   exceções do domínio (404, 409, 400, 500)
├── handler     @RestControllerAdvice que devolve todo erro num JSON padronizado
└── config      CORS, ModelMapper e codificador de senha
src/main/resources/db/resolveai_api_oracle.sql   script de criação das tabelas, sequences e dados de exemplo
```

## Como rodar

### 1. Banco de dados

No Oracle SQL Developer, conectado ao schema do grupo, abra `src/main/resources/db/resolveai_api_oracle.sql` e execute como script (F5). Ele apaga e recria as tabelas `tb_*` e as sequences `sq_tb_*`, então pode ser rodado mais de uma vez.

### 2. Variáveis de ambiente

As credenciais nunca ficam no código. A API lê estas variáveis:

| Variável | Para que serve |
|---|---|
| `DB_URL` | URL JDBC do Oracle |
| `DB_USER` | usuário do banco |
| `DB_PASSWORD` | senha do banco |
| `CORS_ORIGINS` | origens liberadas para o front, separadas por vírgula (padrão: `http://localhost:5173`) |
| `PORT` | porta da API (padrão: `8080`) |

Para rodar local, copie `.env.example` para `.env` e preencha (o `.env` está no `.gitignore`).

### 3a. Com Maven

```bash
export DB_URL=... DB_USER=... DB_PASSWORD=...
mvn spring-boot:run
```

### 3b. Com Docker

```bash
docker compose up --build
```

ou

```bash
docker build -t resolveai-api .
docker run -p 8080:8080 --env-file .env resolveai-api
```

A API sobe em `http://localhost:8080`.

## Endpoints

| Método | Rota | Sucesso | Erros |
|---|---|---|---|
| GET | `/categorias` | 200 | — |
| GET | `/categorias/{id}` | 200 | 404 |
| POST | `/categorias` | 201 + Location | 400 · 409 |
| PUT | `/categorias/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/categorias/{id}` | 204 | 404 · 409 |
| GET | `/bairros` | 200 | — |
| GET | `/usuarios` | 200 | — |
| GET | `/usuarios/{id}` | 200 | 404 |
| GET | `/usuarios/{id}/ocorrencias` | 200 | 404 |
| POST | `/usuarios` | 201 + Location | 400 · 409 |
| PUT | `/usuarios/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/usuarios/{id}` | 204 | 404 · 409 |
| GET | `/ocorrencias?status=&categoria=&bairro=` | 200 | 400 |
| GET | `/ocorrencias/parecidas?cdCategoria=&latitude=&longitude=` | 200 | 400 |
| GET | `/ocorrencias/{id}` | 200 | 404 |
| POST | `/ocorrencias` | 201 + Location | 400 · 404 · 409 |
| PUT | `/ocorrencias/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/ocorrencias/{id}` | 204 | 404 · 409 |
| GET | `/ocorrencias/{id}/historico` | 200 | 404 |
| POST | `/ocorrencias/{id}/historico` | 201 + Location | 400 · 404 · 409 |
| GET | `/historico/{id}` | 200 | 404 |
| PUT | `/historico/{id}` | 200 | 400 · 404 |
| DELETE | `/historico/{id}` | 204 | 404 |
| GET | `/ocorrencias/{id}/apoios` | 200 | 404 |
| POST | `/ocorrencias/{id}/apoios` | 201 + Location | 400 · 404 · 409 |
| DELETE | `/ocorrencias/{id}/apoios/{idUsuario}` | 204 | 404 |
| POST | `/ocorrencias/{id}/denuncias` | 201 + Location | 400 · 404 · 409 |
| GET | `/denuncias?status=PENDENTE` | 200 | 400 |
| GET | `/denuncias/{id}` | 200 | 404 |
| PUT | `/denuncias/{id}` | 200 | 400 · 404 · 409 |
| DELETE | `/denuncias/{id}` | 405 | a decisão da moderação não pode ser apagada |
| GET | `/placar?categoria=&inicio=&fim=` | 200 | 400 |

O relacionamento 1:N principal é **Ocorrência → Histórico de status** (`/ocorrencias/{id}/historico`). Também são 1:N: Ocorrência → Apoios, Ocorrência → Denúncias e Usuário → Ocorrências.

### Exemplo: registrar uma ocorrência

`POST /ocorrencias`

```json
{
  "cdUsuario": 1,
  "cdCategoria": 2,
  "cdBairro": 9,
  "titulo": "Calçada quebrada em frente à escola",
  "descricao": "Placa de concreto solta e buraco no meio da calçada.",
  "endereco": "Rua Vergueiro, 1180",
  "latitude": -23.5881,
  "longitude": -46.6362,
  "registrarMesmoAssim": false
}
```

Resposta `201 Created`, com o header `Location: /ocorrencias/{id}`. O autor não aparece na resposta pública.

### Formato dos erros

Todo erro sai neste JSON, sem stack trace nem SQL:

```json
{
  "timestamp": "2026-11-19T21:45:10",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Você já apoia esta ocorrência.",
  "caminho": "/ocorrencias/5/apoios",
  "campos": []
}
```

No 400 de validação, `campos` lista cada campo inválido com a sua mensagem.

## Regras de negócio

- **Apoio ("Eu também vi"):** só quem está a até 500 m do local pode apoiar. O autor não apoia a própria ocorrência, cada usuário apoia uma vez só, e ocorrência resolvida ou rejeitada não recebe apoio. Com 2 apoios, uma ocorrência ABERTA passa para CONFIRMADA automaticamente.
- **Ocorrência parecida:** ao registrar, se já existe uma ocorrência aberta da mesma categoria a até 30 m, a API devolve 409 e sugere apoiar a existente. Com `registrarMesmoAssim: true`, registra assim mesmo.
- **Status:** as mudanças seguem um fluxo válido (ex.: REJEITADA não muda mais, RESOLVIDA só pode ser REABERTA). Quando um morador informa que foi resolvido ou contesta, precisa estar a até 30 m do local.
- **Edição:** ocorrência RESOLVIDA ou REJEITADA não pode ser editada.
- **Integridade:** não dá para remover categoria, usuário ou ocorrência que tenha registros ligados; e-mail e nome de categoria não se repetem.
- **Denúncia e moderação:** cada usuário denuncia uma publicação uma vez; com 3 denúncias pendentes a ocorrência sai do mapa até a decisão do moderador. A decisão (MANTIDA ou REMOVIDA) não pode ser alterada nem apagada.
- **Cadastro:** a senha é guardada com hash BCrypt e nunca volta nas respostas.

## Deploy

A API roda no Render a partir do `Dockerfile` multi-stage deste repositório: o primeiro estágio compila com Maven e o segundo roda só o `.jar` numa imagem JRE, com usuário não-root. As variáveis `DB_URL`, `DB_USER`, `DB_PASSWORD` e `CORS_ORIGINS` são configuradas no painel do Render.

> O plano gratuito do Render "dorme" depois de um tempo parado. A primeira requisição pode demorar cerca de 1 minuto.
