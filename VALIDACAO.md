# Validação executada

Data: 22/09/2026.

## Validação manual confirmada pelo usuário

Em 22/09/2026, João Mocock confirmou que realizou a validação do projeto e que todas as funcionalidades testadas funcionam corretamente, incluindo o uso da interface e o cancelamento de agendamentos após a correção disponibilizada.

Essa confirmação corresponde à execução manual no ambiente do usuário. Ela complementa os resultados técnicos abaixo; não representa uma nova execução dos testes automatizados pelo assistente nem validação de Oracle, Docker ou GitHub Actions.

## Resultados dos testes técnicos anteriores

- `mvn clean verify`: **BUILD SUCCESS**.
- **14 testes executados; 0 falhas; 0 erros; 0 ignorados.**
- **30 verificações adicionais por HTTP real aprovadas**, executando o JAR com H2 em arquivo, incluindo oito solicitações concorrentes e reinício do processo.
- Compilação e testes com JDK 17.0.16 e Maven 3.9.9.
- JAR executável iniciado com Java 17.0.20; servidor HTTP e H2 em arquivo iniciados com sucesso.
- Cadastro via HTTP, encerramento normal do processo e reinício usando o mesmo banco: o registro permaneceu disponível.
- Página inicial servida por HTTP e sintaxe do JavaScript verificada por `node --check`.

## Cobertura dos testes

1. Cadastro e listagem de paciente.
2. Rejeição de nome em branco.
3. Criação com status AGENDADO e cabeçalho Location.
4. Rejeição de data passada e do instante presente.
5. Conflito de horário, inclusive entre offsets equivalentes.
6. Profissionais diferentes no mesmo horário.
7. Cancelamento com preservação de registro/motivo e liberação do horário.
8. Motivo obrigatório e preservação do motivo original em segundo cancelamento.
9. Filtros por paciente, profissional e status, isolados e combinados.
10. Respostas 404 para referências inexistentes.
11. Rejeição de UUID/status/JSON inválidos, campos ausentes e segundos não zerados.
12. Duas transações simultâneas: uma reserva aceita e outra em conflito, com um único registro persistido.

13. Content-Type inválido retorna 415 em JSON, sem erro interno.
14. Limites de nome/tipo/motivo e exigência de fuso; cancelamento inválido não altera status.

## Correção encontrada na revalidação

A primeira bateria adicional revelou resposta 500 para Content-Type text/plain. Foi adicionado tratamento específico para retornar 415 em JSON, com teste de regressão. Após a correção, a compilação completa, os 14 testes e as 30 verificações HTTP passaram.

## Publicação e limites da validação

- A validação manual da interface foi confirmada pelo usuário. A automação de ponta a ponta em navegador pelo assistente não foi concluída, pois o download do Chromium ficou indisponível no ambiente.
- Docker e GitHub Actions foram configurados; sua execução não foi verificada nesta atualização.
- Oracle não implementado nem testado.
- Repositório publicado e acessível em https://github.com/JoaoMocock/clinica-api. A publicação foi confirmada pela leitura deste arquivo diretamente no repositório.
- A execução manual foi confirmada pelo candidato. Não foi registrada uma revisão linha a linha do código nem uma validação específica dos diferenciais não implementados.

## Ajustes do ambiente de testes

O ambiente inicial tinha somente Java Runtime e não possuía Maven. Foram usados JDK e Maven temporários, com proxy e certificados do ambiente para obter as dependências. Esses ajustes não foram incluídos no projeto. O mecanismo padrão de agentes do Mockito não funcionou no ambiente restrito; a configuração de subclasses foi adicionada aos recursos de teste, e a suíte completa foi executada novamente com sucesso.
