# Clínica API

Teste prático de desenvolvedor júnior: API REST para pacientes e agendamentos, com Java 17, Spring Boot 3.5.16, Spring JDBC e banco relacional H2 persistido em arquivo. Inclui interface HTML/JavaScript sem dependências de frontend.

## Executar

Pré-requisitos: **JDK 17** e **Maven 3.6.3 ou superior**. A primeira execução requer internet para baixar as dependências.

```bash
mvn clean verify
mvn spring-boot:run
```

Abra http://localhost:8080 para usar a interface. Cadastre um paciente e um profissional antes de criar uma consulta. O banco é criado automaticamente em `data/clinica.mv.db` e os dados sobrevivem ao reinício da aplicação. Não execute duas instâncias sobre o mesmo arquivo H2.

O ZIP também inclui o executável pronto em `dist/clinica-api-1.0.0.jar`. Para usá-lo, basta Java 17 e `java -jar dist/clinica-api-1.0.0.jar`. Não é necessário Maven nesse caso.

Para executar o pacote após compilar o código:

```bash
java -jar target/clinica-api-1.0.0.jar
```

Alternativa com Docker (não exige Java/Maven na máquina):

```bash
docker build -t clinica-api .
docker run --rm -p 8080:8080 -v clinica-dados:/app/data clinica-api
```

O comando de build do Docker também executa os testes. A imagem Docker não foi construída neste ambiente; consulte `VALIDACAO.md` para o que foi efetivamente verificado.

## Endpoints

Todos os endpoints abaixo recebem/retornam JSON. Para POST/PATCH, use `Content-Type: application/json`.

| Método | Rota | Resultado |
|---|---|---|
| POST | `/api/pacientes` | Cadastra paciente (201) |
| GET | `/api/pacientes` | Lista pacientes por nome (200) |
| POST | `/api/profissionais` | Cadastra profissional (201) |
| GET | `/api/profissionais` | Lista profissionais por nome (200) |
| POST | `/api/agendamentos` | Cria consulta (201 + Location) |
| GET | `/api/agendamentos` | Lista por data/hora (200) |
| GET | `/api/agendamentos/{id}` | Consulta um registro (200) |
| PATCH | `/api/agendamentos/{id}/cancelamento` | Cancela mantendo o registro (200) |

Corpo para cadastro de paciente ou profissional:

```json
{"nome":"Ana Silva"}
```

Resposta de cadastro: `{"id":"UUID gerado pelo servidor","nome":"Ana Silva"}`. Nomes não são identificadores únicos; pessoas homônimas são permitidas.

Corpo de novo agendamento (substitua os IDs pelos valores retornados nos cadastros e escolha uma data futura):

```json
{
  "pacienteId": "d164759a-cfd6-4a91-8ef0-54f875e97771",
  "profissionalId": "e4d048a8-b56d-4cec-ae29-a7f08a5878e2",
  "dataHora": "2030-12-10T09:30:00-03:00",
  "tipoAtendimento": "Consulta"
}
```

A resposta contém `id`, `pacienteId`, `profissionalId`, `dataHora` em UTC, `tipoAtendimento`, `status: "AGENDADO"` e `motivoCancelamento: null`.

Corpo do cancelamento:

```json
{"motivo":"Paciente solicitou remarcação."}
```

O mesmo registro passa a `CANCELADO` e recebe `motivoCancelamento`. Cancelar novamente retorna 409 e mantém o primeiro motivo. Não há exclusão de agendamentos.

### Exemplo com curl

```bash
curl -i -X POST http://localhost:8080/api/pacientes \
  -H 'Content-Type: application/json' -d '{"nome":"Ana Silva"}'
curl -i -X POST http://localhost:8080/api/profissionais \
  -H 'Content-Type: application/json' -d '{"nome":"Dr. Paulo"}'
curl http://localhost:8080/api/agendamentos?status=AGENDADO
```

### Filtros

`GET /api/agendamentos?pacienteId={uuid}&profissionalId={uuid}&status=CANCELADO`

Cada filtro é opcional e pode ser usado isoladamente. Quando combinados, todos precisam ser atendidos (AND). Status aceitos: `AGENDADO` e `CANCELADO`, em maiúsculas. Sem resultados: `[]`. A listagem não tem paginação neste escopo.

### Validações e erros

- Nome obrigatório, não vazio, até 120 caracteres.
- IDs obrigatórios em formato UUID e referenciando registros existentes.
- Data/hora estritamente futura, ISO 8601 com offset (`Z` ou `-03:00`, por exemplo).
- Precisão de minutos: segundos e frações devem ser zero.
- Tipo de atendimento obrigatório, texto de até 80 caracteres.
- Motivo do cancelamento obrigatório, não vazio, até 500 caracteres.
- Campos JSON desconhecidos são rejeitados para evidenciar erros de digitação.

| Código | Significado |
|---|---|
| 400 | Dados inválidos, data passada, motivo ausente ou formato incorreto |
| 404 | Paciente, profissional ou agendamento não encontrado |
| 409 | Horário ocupado ou agendamento já cancelado |
| 405 | Método HTTP não permitido |
| 500 | Erro interno sem exposição de detalhes ao cliente |

Formato de erro:

```json
{"status":409,"mensagem":"Profissional já possui agendamento neste horário.","timestamp":"2030-01-01T00:00:00Z"}
```

## Modelagem e estrutura

- `ClinicaController`: contrato HTTP e validação dos DTOs.
- `AgendamentoService`: regras de criação/cancelamento e transações.
- `ClinicaRepository`: SQL parametrizado e mapeamento dos registros.
- `Models`: DTOs imutáveis e enum de status.
- `ApiErrorHandler`: respostas de erro padronizadas.
- `schema.sql`: tabelas, chaves estrangeiras, índices e restrições.
- `static/index.html`: interface que consome a própria API.
- `ClinicaIntegrationTest`: testes com Spring, HTTP simulado e H2 real em memória.

Paciente e profissional têm identidade própria. A coluna interna `horario_ativo` contém a combinação de profissional e instante UTC, com restrição UNIQUE. No cancelamento ela recebe NULL, liberando o horário. Isso impede reservas simultâneas do mesmo horário no banco, sem depender de uma consulta prévia sujeita a corrida. Vários registros cancelados podem manter o mesmo profissional e horário.

Interpretação adotada: o conflito vale para consultas **ativas**. Consultas canceladas são histórico e não ocupam a agenda. O enunciado não define duração; portanto não se calcula sobreposição entre horários diferentes. A granularidade adotada é de um minuto.

## Testes

```bash
mvn clean verify
```

Os testes usam relógio fixo e banco em memória isolado do arquivo da aplicação. Cobrem cadastro, campos inválidos, passado/presente, duplicidade, fusos equivalentes, profissionais distintos, cancelamento e motivo, liberação do horário, filtros isolados/combinados, referências inexistentes e concorrência entre duas transações. Resultados em `target/surefire-reports/`.

O arquivo `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` seleciona o mecanismo de subclasses para evitar autoanexação de agente JVM em ambientes restritos. Os testes não substituem o banco nem o serviço por mocks.

O workflow `.github/workflows/ci.yml` executa a validação a cada push e pull request após a publicação no GitHub.

## Publicação no GitHub

**Pendente:** este pacote não representa um repositório já publicado. A criação do repositório e o link de entrega exigem acesso à conta GitHub.

O ZIP inclui `historico.bundle` com os commits reais da implementação. Para recuperar o projeto e seu histórico, execute a partir da pasta que contém o bundle, escolhendo um destino que ainda não exista:

```bash
git clone historico.bundle clinica-api-git
cd clinica-api-git
git remote remove origin
git branch -M main
```

Com GitHub CLI instalado e autenticado, crie um repositório privado (o padrão abaixo preserva o conteúdo até você decidir compartilhá-lo):

```bash
gh auth login
gh repo create clinica-api --private --source=. --remote=origin --push
gh repo view --json url --jq .url
```

Compartilhe o acesso com o avaliador se o repositório permanecer privado. Se criar um repositório vazio pelo site, use a URL real informada pelo GitHub em `git remote add origin URL_DO_REPOSITORIO`, seguido de `git push -u origin main`. Não inicialize outro README no repositório remoto.

## Limites do escopo

Sem autenticação, autorização, prontuário, notificações, edição de consultas ou paginação. A interface é uma demonstração; não utilizar com dados clínicos reais nem publicar como serviço de produção sem esses controles. H2 foi escolhido para execução simples. Oracle não foi implementado nem validado; o diferencial é opcional e não se declara compatibilidade somente por usar SQL/JDBC. Evolução para Oracle exigiria driver, configuração, migração do DDL e testes no banco real.

Consulte `DECISOES.md` para as decisões e a declaração de uso de IA, e `VALIDACAO.md` para as verificações efetivamente executadas.
