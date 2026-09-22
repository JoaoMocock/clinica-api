CREATE TABLE IF NOT EXISTS paciente (
 id VARCHAR(36) PRIMARY KEY, nome VARCHAR(120) NOT NULL
);
CREATE TABLE IF NOT EXISTS profissional (
 id VARCHAR(36) PRIMARY KEY, nome VARCHAR(120) NOT NULL
);
CREATE TABLE IF NOT EXISTS agendamento (
 id VARCHAR(36) PRIMARY KEY,
 paciente_id VARCHAR(36) NOT NULL REFERENCES paciente(id),
 profissional_id VARCHAR(36) NOT NULL REFERENCES profissional(id),
 data_hora TIMESTAMP WITH TIME ZONE NOT NULL,
 tipo_atendimento VARCHAR(80) NOT NULL,
 status VARCHAR(10) NOT NULL CHECK (status IN ('AGENDADO', 'CANCELADO')),
 motivo_cancelamento VARCHAR(500),
 horario_ativo VARCHAR(100) UNIQUE,
 CHECK ((status = 'AGENDADO' AND motivo_cancelamento IS NULL AND horario_ativo IS NOT NULL)
 OR (status = 'CANCELADO' AND motivo_cancelamento IS NOT NULL AND horario_ativo IS NULL))
);
CREATE INDEX IF NOT EXISTS idx_agendamento_paciente ON agendamento(paciente_id);
CREATE INDEX IF NOT EXISTS idx_agendamento_profissional ON agendamento(profissional_id);
