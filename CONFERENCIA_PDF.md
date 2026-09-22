# Conferência do teste prático

Documento-base: DOC-20260922-WA0043.pdf (2 páginas). Conferência realizada em 22/09/2026.

## Conclusão

A API atende às operações e regras de negócio testadas. Build completo aprovado, 14 testes automatizados aprovados e 30 verificações por HTTP real aprovadas após uma correção no tratamento de Content-Type. A entrega ainda NÃO está 100% concluída: o repositório não foi publicado no GitHub e seu link obrigatório não existe.

## Todos os itens do enunciado

| Item solicitado | Resultado | Evidência / observação |
|---|---|---|
| Agendamento com paciente, profissional, data/hora, tipo e status | Atendido | DTO, tabela e resposta HTTP de criação conferidos |
| Cadastrar paciente | Atendido | POST /api/pacientes retorna 201 com UUID |
| Listar pacientes | Atendido | GET /api/pacientes retorna JSON |
| Criar agendamento | Atendido | POST /api/agendamentos, status inicial AGENDADO |
| Listar agendamentos | Atendido | GET /api/agendamentos |
| Cancelar agendamento | Atendido | PATCH /api/agendamentos/{id}/cancelamento |
| Impedir dois agendamentos de um profissional no mesmo horário | Atendido para consultas ativas | Restrição UNIQUE; oito pedidos simultâneos produziram uma reserva e sete conflitos |
| Impedir data/hora passada | Atendido | Rejeição 400; testes também cobrem instante presente |
| Registrar motivo de cancelamento | Atendido | Motivo obrigatório; ausente, nulo, vazio, espaços e excesso de tamanho rejeitados |
| Alterar status para CANCELADO e manter registro | Atendido | GET após cancelamento mantém ID e motivo; dados permanecem após reinício |
| Filtro por paciente, profissional ou status | Atendido | Testes isolados e combinados; combinação usa AND |
| Java e Spring Boot | Atendido | Java 17, Spring Boot 3.5.16; build executado |
| API REST retornando JSON | Atendido | Respostas de sucesso e erros verificadas |
| Banco relacional | Atendido | H2 em arquivo, chaves estrangeiras e restrições |
| Pelo menos um teste automatizado de regra de negócio | Atendido | 14 métodos de teste, incluindo regras obrigatórias |
| Tratamento básico de erros | Atendido após correção | Códigos 400, 404, 409, 405 e 415; erro interno genérico sem detalhes no cliente |
| Projeto versionado no GitHub | PENDENTE obrigatório | Histórico Git local existe, mas não foi publicado |
| Link do repositório | PENDENTE obrigatório | Nenhuma URL foi inventada |
| README.md com execução | Atendido | Maven, JAR, Docker, endpoints e exemplos |
| DECISOES.md: decisões técnicas | Atendido | Seção própria e respostas diretas |
| DECISOES.md: prioridades e exclusões | Atendido | Escopo e limites explícitos |
| DECISOES.md: uso de IA e validação | Atendido | Declara uso extenso de IA e diferencia validação técnica de revisão pessoal |
| Commits mostrando evolução | Atendido localmente | Commits de API, testes, interface/CI, ajustes, documentação e correção desta revisão; bundle incluído |
| Informar claramente o que não foi finalizado | Atendido | GitHub, Oracle e limitações de validação documentados |
| Prazo sugerido de 48 horas | Não verificável | O PDF não informa início nem data-limite; não é possível confirmar entrega dentro do prazo |

## Diferenciais opcionais

| Diferencial | Situação |
|---|---|
| Boas práticas de modelagem | Relações por UUID, chaves estrangeiras, restrições e chave única para reserva ativa; revisão de código realizada |
| Validações consistentes | DTOs validados, limites de texto, referências existentes e formato temporal testados |
| Estrutura limpa | Controller, service, repository, DTOs e tratamento de erros separados; critério qualitativo sujeito à avaliação do recrutador |
| Testes adicionais | Incluídos e executados |
| Documentação de endpoints | Incluída no README |
| Angular ou outra interface simples | HTML/JavaScript implementado e servido por HTTP; Angular não é obrigatório; interação visual ainda não validada em navegador |
| Oracle ou demonstração de compatibilidade | Não implementado; não impede atendimento dos requisitos mínimos, pois é diferencial |

## Interpretações que o avaliador deve conhecer

- Consultas canceladas não bloqueiam o horário. O PDF não explicita essa exceção: a solução interpreta o conflito como aplicável às consultas ativas e preserva os cancelamentos no histórico. Se o avaliador exigir bloqueio mesmo após cancelamento, essa regra precisará ser ajustada.
- O sistema adota precisão de minutos e exige fuso horário. São decisões adicionais documentadas; o PDF não define granularidade.
- A data precisa ser estritamente futura. O instante presente também é rejeitado.
- Não é calculada sobreposição por duração; o enunciado exige apenas impedir o mesmo horário e não define duração das consultas.
- Repetir cancelamento retorna 409 e não sobrescreve o motivo original.

## O que não foi comprovado

Fluxo visual completo em navegador, execução de Docker, execução do workflow no GitHub, Oracle, revisão pelo próprio candidato e cumprimento do prazo sugerido. Não se declara ausência de qualquer bug: os resultados comprovam somente os cenários descritos.

## Resultado das 30 verificações HTTP

- APROVADO: Cadastro paciente JSON.
- APROVADO: Cadastro profissional.
- APROVADO: Lista pacientes.
- APROVADO: Cinco campos obrigatórios e status inicial.
- APROVADO: Duplicidade.
- APROVADO: Fuso equivalente.
- APROVADO: Passado.
- APROVADO: Filtro pacienteId.
- APROVADO: Filtro profissionalId.
- APROVADO: Filtro status.
- APROVADO: Motivo ausente.
- APROVADO: Motivo nulo.
- APROVADO: Motivo vazio.
- APROVADO: Motivo espaços.
- APROVADO: Motivo longo.
- APROVADO: Cancelamento com motivo.
- APROVADO: Registro preservado.
- APROVADO: Segundo cancelamento não sobrescreve.
- APROVADO: Reutilização do horário cancelado.
- APROVADO: nome vazio.
- APROVADO: nome longo.
- APROVADO: campo desconhecido.
- APROVADO: Data sem fuso.
- APROVADO: Tipo vazio.
- APROVADO: Tipo longo.
- APROVADO: Método inválido.
- APROVADO: Content-Type inválido retorna 415.
- APROVADO: Página inicial.
- APROVADO: 8 reservas simultâneas: uma aceita e sete conflitos.
- APROVADO: Consultas e motivo persistem após reinício.
