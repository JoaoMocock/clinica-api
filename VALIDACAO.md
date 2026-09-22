# Validação executada

Data: 22/09/2026.

## Resultados confirmados

- `mvn clean verify`: **BUILD SUCCESS**.
- **12 testes executados; 0 falhas; 0 erros; 0 ignorados.**
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

## Limitações explícitas

- Verificação visual e fluxo interativo em navegador não concluídos: o download do Chromium ficou indisponível no ambiente. O frontend foi inspecionado no código, teve a sintaxe verificada e foi servido pela aplicação; isso não equivale a teste de ponta a ponta no navegador.
- Docker e GitHub Actions foram configurados, mas não executados nesses serviços.
- Oracle não implementado nem testado.
- GitHub não publicado: depende da conexão da conta. O pacote inclui histórico Git recuperável em `historico.bundle`.
- A revisão e execução pelo próprio candidato não foram confirmadas.

## Ajustes do ambiente de testes

O ambiente inicial tinha somente Java Runtime e não possuía Maven. Foram usados JDK e Maven temporários, com proxy e certificados do ambiente para obter as dependências. Esses ajustes não foram incluídos no projeto. O mecanismo padrão de agentes do Mockito não funcionou no ambiente restrito; a configuração de subclasses foi adicionada aos recursos de teste, e a suíte completa foi executada novamente com sucesso.
