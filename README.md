# Desconfie · API Java

API REST do **Desconfie**, solução da Global Solution 2026/2 (FIAP, 1TDSPJ, tema CitizenTech).

O Desconfie ajuda quem recebe mensagens suspeitas, principalmente pessoas idosas, a descobrir se aquilo é golpe antes de agir. A pessoa cola o texto, envia o print da conversa ou o link e recebe um **semáforo**:

- 🟢 **VERDE**: nada suspeito.
- 🟡 **AMARELO**: tenha cuidado.
- 🔴 **VERMELHO**: é golpe.

Junto com o semáforo vem a orientação do que fazer. Cada usuário pode montar um **círculo de proteção**: um guardião e até 3 dependentes. Quando alguém do círculo recebe um VERMELHO, os outros membros são avisados.

A nota de risco junta duas fontes:

- **Regras (esta API):** padrões de golpe cadastrados no banco, como "troquei de número", "pix" ou "central de segurança", e sinais de links falsos: domínio que imita marca, TLD de risco, encurtador, IP, `@`, falta de HTTPS.
- **IA (API Python do grupo):** um modelo de classificação treinado com base pública. Também lê o texto dos prints (OCR).

A nota final é a média das duas. Se a IA estiver fora do ar, a API continua funcionando só com as regras.

## Integrantes

| Nome | RM |
|---|---|
| Juan Souza Marques | 573469 |
| Lucas Leite Carlos | 571985 |
| Matheus Matsushita de Souza | 570017 |
| Pedro Amaro Pires | 570636 |

## Tecnologias

- Java 25 e Spring Boot 4.1.2 (Spring Web)
- Oracle Database, com **JDBC puro** (DataSource + PreparedStatement)
- Bean Validation para validar a entrada
- ModelMapper para converter DTO ↔ model
- Lombok
- SpringDoc OpenAPI (Swagger)
- Docker e Docker Compose

## Arquitetura

```
Front-end ──HTTP/JSON──> Controller ──> Service ──> DAO ──JDBC──> Oracle
                                          │
                                          └──HTTP/JSON──> API Python (IA)
```

| Pacote | Responsabilidade |
|---|---|
| `controller` | Recebe as requisições, valida com `@Valid` e devolve o status HTTP correto |
| `service` | Regras de negócio (RN01 a RN09), motor de regras e chamada à IA |
| `dao` | Acesso ao Oracle com `PreparedStatement`, ids gerados pelas sequences |
| `model` | Entidades do domínio |
| `dto` | Objetos de entrada (`request`) e de saída (`response`); a API nunca expõe o model |
| `exception` / `handler` | Exceções de negócio e o `GlobalExceptionHandler`, que devolve todo erro no mesmo JSON |
| `config` | DataSource, ModelMapper, cliente HTTP da IA e CORS |
| `enums` | Tipos fixos: tipo de entrada, tipo de padrão, nível de risco e feedback |

## Como executar

### 1. Banco de dados

No Oracle SQL Developer, execute os scripts da pasta `sql/` nesta ordem:

1. `01_ddl.sql`: sequences, tabelas e comentários
2. `02_inserts.sql`: dados iniciais, incluindo o catálogo de categorias e padrões de golpe usado pelo motor de regras
3. `03_consultas.sql`: consultas de exemplo (opcional para a API)

O arquivo `desconfie_bd_entrega.sql` reúne os três, com uma limpeza no início, e pode ser executado inteiro.

### 2. Variáveis de ambiente

Nenhuma credencial fica no código. A API lê tudo de variáveis de ambiente:

| Variável | Obrigatória | Exemplo |
|---|---|---|
| `DB_URL` | sim | `jdbc:oracle:thin:@<host>:<porta>:<sid>` |
| `DB_USER` | sim | usuário do Oracle |
| `DB_PASSWORD` | sim | senha do Oracle |
| `IA_URL` | não | URL da API Python (padrão `http://localhost:5000`) |
| `PORT` | não | porta da API (padrão `8080`) |

### 3a. Rodando com Docker

```bash
cp .env.example .env      # preencha os valores no .env
docker compose up --build
```

### 3b. Rodando com o Maven Wrapper

Defina as variáveis de ambiente e execute:

```bash
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows
```

No IntelliJ, as variáveis podem ser definidas em **Run → Edit Configurations → Environment variables**.

A API sobe em `http://localhost:8080`. Os links úteis:

- Documentação Swagger: `http://localhost:8080/swagger-ui/index.html`
- Saúde da API e da IA: `GET http://localhost:8080/health`

### Testes

```bash
./mvnw test
```

## Endpoints

### Usuários
| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| POST | `/usuarios` | Cadastra um usuário | 201 + Location |
| POST | `/usuarios/login` | Login com e-mail e senha | 200 |
| GET | `/usuarios` | Lista os usuários | 200 |
| GET | `/usuarios/{id}` | Busca um usuário | 200 |
| PUT | `/usuarios/{id}` | Atualiza nome, e-mail e telefone | 200 |
| DELETE | `/usuarios/{id}` | Remove um usuário | 204 |

### Círculo de proteção
| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| POST | `/usuarios/{id}/dependentes` | Adiciona um dependente pelo e-mail | 201 + Location |
| GET | `/usuarios/{id}/dependentes` | Lista os dependentes do guardião | 200 |
| DELETE | `/usuarios/{id}/dependentes/{dependenteId}` | Desfaz o vínculo | 204 |
| GET | `/usuarios/{id}/guardiao` | Mostra o guardião do usuário | 200 |

### Categorias e padrões de golpe
| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| POST | `/categorias` | Cadastra uma categoria | 201 + Location |
| GET | `/categorias` | Lista as categorias | 200 |
| GET | `/categorias/{id}` | Busca uma categoria | 200 |
| PUT | `/categorias/{id}` | Atualiza uma categoria | 200 |
| DELETE | `/categorias/{id}` | Remove uma categoria | 204 |
| GET | `/categorias/{id}/padroes` | Lista os padrões da categoria | 200 |
| POST | `/categorias/{id}/padroes` | Cadastra um padrão na categoria | 201 + Location |
| POST | `/padroes` | Cadastra um padrão (inclusive domínio oficial, sem categoria) | 201 + Location |
| GET | `/padroes` | Lista os padrões | 200 |
| GET | `/padroes/{id}` | Busca um padrão | 200 |
| PUT | `/padroes/{id}` | Atualiza um padrão | 200 |
| DELETE | `/padroes/{id}` | Remove um padrão | 204 |

### Análises
| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| POST | `/usuarios/{id}/analises` | Analisa um texto, link ou imagem | 201 + Location |
| GET | `/usuarios/{id}/analises` | Histórico do usuário | 200 |
| GET | `/usuarios/{id}/circulo/analises` | Histórico AMARELO e VERMELHO de todo o círculo | 200 |
| GET | `/analises` | Lista todas as análises | 200 |
| GET | `/analises/{id}` | Detalhe com padrões encontrados e sinais do link | 200 |
| PUT | `/analises/{id}` | Registra o feedback (`ERA_GOLPE` ou `FALSO_ALARME`) | 200 |
| DELETE | `/analises/{id}` | Remove uma análise | 204 |

### Alertas
| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| GET | `/usuarios/{id}/alertas?naoLidos=true` | Alertas recebidos pelo usuário | 200 |
| PATCH | `/alertas/{id}/lido` | Marca o alerta como lido | 200 |

### Saúde
| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| GET | `/health` | Mostra se a API e a IA estão respondendo | 200 |

### Exemplo de análise

Requisição:

```http
POST /usuarios/5/analises
Content-Type: application/json

{ "tipo": "TEXTO", "conteudo": "Mãe, troquei de número. Preciso de um Pix urgente de R$ 800." }
```

Resposta (resumida):

```json
{
  "id": 13,
  "tipo": "TEXTO",
  "notaRegras": 80,
  "notaIa": 92,
  "notaFinal": 86,
  "risco": "VERMELHO",
  "iaDisponivel": true,
  "categoriaPrincipal": "Falso parente",
  "orientacao": "Não faça nenhum Pix. Ligue para o número antigo da pessoa ou confirme com outro familiar antes de qualquer coisa.",
  "padroesEncontrados": [ { "termo": "troquei de numero", "peso": 35 } ],
  "alertasGerados": 3
}
```

## Regras de negócio

| Regra | Descrição | Resposta |
|---|---|---|
| RN01 | Um guardião tem no máximo 3 dependentes | 409 |
| RN02 | Cada dependente tem um único guardião, e ninguém é guardião de si mesmo | 409 |
| RN03 | O e-mail é único | 409 |
| RN04 | Nota final = média de regras e IA; sem IA, só regras. Abaixo de 30 VERDE, de 30 a 59 AMARELO, 60 ou mais VERMELHO | — |
| RN05 | Uma análise VERMELHA gera alerta para todos os outros membros do círculo | — |
| RN06 | Não é possível remover um usuário, categoria ou padrão que já tem análises ligadas | 409 |
| RN07 | Análise de imagem depende da API Python para ler o texto (OCR); se ela estiver fora do ar, a resposta é 503 | 503 |
| RN08 | Cada padrão soma pontos uma vez só, mesmo que apareça várias vezes; a nota das regras vai até 100 | — |
| RN09 | O histórico do círculo mostra só as análises AMARELAS e VERMELHAS dos membros | — |

## Erros

Todo erro volta no mesmo formato, sem stack trace nem SQL:

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos. Confira os campos destacados.",
  "caminho": "/usuarios",
  "timestamp": "2026-11-17T10:15:30",
  "campos": [ { "campo": "email", "mensagem": "informe um e-mail válido" } ]
}
```

| Status | Quando |
|---|---|
| 400 | Dados inválidos, JSON malformado ou id em formato errado |
| 401 | E-mail ou senha incorretos no login |
| 404 | Recurso ou rota inexistente |
| 405 | Método HTTP não suportado na rota |
| 409 | Regra de negócio violada ou remoção de registro com filhos |
| 503 | Análise de imagem com a IA indisponível |
| 500 | Erro inesperado (mensagem genérica) |

## Banco de dados

Sete tabelas com o prefixo `tb_dsf_`:

| Tabela | Relacionamento |
|---|---|
| `tb_dsf_usuario` | Pessoa cadastrada |
| `tb_dsf_vinculo_guardiao` | Autorrelacionamento usuário × usuário (guardião → dependentes) |
| `tb_dsf_categoria_golpe` | Tipo de golpe e a orientação ao usuário |
| `tb_dsf_padrao_golpe` | 1:N com categoria; termos e sinais com peso |
| `tb_dsf_analise` | 1:N com usuário; cada verificação feita |
| `tb_dsf_analise_padrao` | N:N entre análise e padrão |
| `tb_dsf_alerta` | 1:N com análise e com usuário destinatário |

## Integração com a IA

A API chama a API Python pela URL em `IA_URL`, com tempo limite de 10 s para conectar e 60 s para responder (o Render gratuito leva cerca de 50 s para acordar):

| Rota da IA | Uso |
|---|---|
| `POST /prever/texto` | Probabilidade de golpe de um texto, convertida em nota de 0 a 100 |
| `POST /prever/url` | Probabilidade de phishing de um link, convertida em nota de 0 a 100 |
| `POST /extrair` | Recebe o print (multipart), lê o texto com OCR e devolve a nota do texto |
| `GET /health` | Verifica se a IA está no ar |

- Se a IA não responder, a análise de texto e de link é feita só com as regras e volta com `"iaDisponivel": false`.
- Na imagem, se o texto foi lido mas o modelo não respondeu, a análise segue só com as regras sobre o texto lido.

## Links

- API publicada: https://java-desconfie-api.onrender.com (Swagger em https://java-desconfie-api.onrender.com/swagger-ui/index.html)
- API Python (IA): https://python-desconfie-2sem.onrender.com
- O plano gratuito do Render desliga o serviço sem uso: o primeiro acesso pode levar cerca de 1 minuto. Abra `/health` antes de usar.
- Repositório: https://github.com/desconfie-globalsolution2026-2sem/java-desconfie-api
- Vídeo: _a definir_
