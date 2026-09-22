package br.com.clinica;

import static br.com.clinica.Models.*;
import static org.springframework.http.HttpStatus.*;

import java.time.*;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendamentoService {
  private final ClinicaRepository repository;
  private final Clock clock;

  public AgendamentoService(ClinicaRepository repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
  }

  @Transactional
  public Agendamento criar(NovoAgendamento pedido) {
    if (!pedido.dataHora().toInstant().isAfter(clock.instant()))
      throw new ApiException(BAD_REQUEST, "A data/hora deve estar no futuro.");
    if (pedido.dataHora().getSecond() != 0 || pedido.dataHora().getNano() != 0)
      throw new ApiException(
          BAD_REQUEST, "Informe o horário com precisão de minutos (segundos iguais a zero).");
    if (!repository.existe(false, pedido.pacienteId()))
      throw new ApiException(NOT_FOUND, "Paciente não encontrado.");
    if (!repository.existe(true, pedido.profissionalId()))
      throw new ApiException(NOT_FOUND, "Profissional não encontrado.");
    var a =
        new Agendamento(
            UUID.randomUUID(),
            pedido.pacienteId(),
            pedido.profissionalId(),
            pedido.dataHora().withOffsetSameInstant(ZoneOffset.UTC),
            pedido.tipoAtendimento().strip(),
            Status.AGENDADO,
            null);
    try {
      repository.inserir(a);
    } catch (DuplicateKeyException e) {
      throw new ApiException(CONFLICT, "Profissional já possui agendamento neste horário.");
    }
    return a;
  }

  @Transactional
  public Agendamento cancelar(UUID id, String motivo) {
    if (motivo == null || motivo.isBlank() || motivo.length() > 500)
      throw new ApiException(BAD_REQUEST, "Informe um motivo de 1 a 500 caracteres.");
    repository
        .buscar(id)
        .orElseThrow(() -> new ApiException(NOT_FOUND, "Agendamento não encontrado."));
    if (!repository.cancelar(id, motivo.strip()))
      throw new ApiException(CONFLICT, "Agendamento já cancelado; motivo original preservado.");
    return repository.buscar(id).orElseThrow();
  }
}
