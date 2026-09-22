# Decisões técnicas

## Quais foram as principais decisões técnicas?

- Java 17 e Spring Boot; divisão simples entre controller, service e repository. Spring JDBC mantém o SQL explícito sem adicionar a complexidade de um ORM para três tabelas.
- H2 em arquivo permite executar sem instalar servidor de banco. UUIDs identificam pacientes, profissionais e consultas; chaves estrangeiras preservam as relações.
- Reserva ativa protegida por chave única no banco, normalizada em UTC. Isso cobre duas requisições concorrentes e fusos que representam o mesmo instante. O cancelamento é transacional, preserva motivo e registro e libera a chave ativa.
- Consultas usam precisão de minutos. A regra se refere ao mesmo instante de início, pois não há duração definida no enunciado. Consultas canceladas deixam de bloquear novas reservas.
- Validação de entrada, SQL parametrizado, status tipado e erros HTTP padronizados. Uma tentativa de cancelar novamente retorna conflito sem alterar o motivo anterior.

## O que foi priorizado e o que ficou de fora?

Prioridade: todas as operações e regras obrigatórias, persistência, testes de integração, documentação e histórico de commits. Como diferenciais, foram incluídos testes adicionais, uma interface HTML/JavaScript e configuração de CI. Profissionais também podem ser cadastrados para evitar IDs fixos ou cadastro manual no banco.

Ficaram de fora Angular, Oracle, login, paginação, notificações, sobreposição por duração e infraestrutura de produção. A configuração Docker foi entregue, mas sua imagem não foi executada no ambiente de preparação. A integração GitHub foi instalada, mas o acesso às ferramentas de publicação não foi disponibilizado nesta conversa; a publicação permanece pendente até existir um link real.

## Utilizou IA? Em quais partes e como validou o resultado?

Sim. O ChatGPT/Codex foi utilizado para interpretar o enunciado, propor a modelagem e gerar código, testes, interface e documentação. O projeto foi preparado com assistência extensa de IA; não se atribui ao candidato uma implementação ou revisão manual que ele ainda não realizou.

A validação técnica executada pelo assistente está descrita em `VALIDACAO.md`. Os testes verificam o comportamento HTTP e as regras com banco real em memória, incluindo uma corrida entre duas transações. O histórico registra as etapas da implementação; não simula trabalho anterior do candidato.

Antes de enviar, o candidato deve executar o projeto na própria máquina, revisar as decisões e compreender o fluxo, especialmente a chave única de reserva e o cancelamento transacional. Essa revisão pessoal ainda não foi confirmada.
