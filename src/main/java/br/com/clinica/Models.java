package br.com.clinica;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class Models {
  private Models() {}

  public enum Status {
    AGENDADO,
    CANCELADO
  }

  public record Pessoa(UUID id, String nome) {}

  public record Cadastro(@NotBlank @Size(max = 120) String nome) {}

  public record NovoAgendamento(
      @NotNull UUID pacienteId,
      @NotNull UUID profissionalId,
      @NotNull OffsetDateTime dataHora,
      @NotBlank @Size(max = 80) String tipoAtendimento) {}

  public record Cancelamento(@NotBlank @Size(max = 500) String motivo) {}

  public record Agendamento(
      UUID id,
      UUID pacienteId,
      UUID profissionalId,
      OffsetDateTime dataHora,
      String tipoAtendimento,
      Status status,
      String motivoCancelamento) {}
}
