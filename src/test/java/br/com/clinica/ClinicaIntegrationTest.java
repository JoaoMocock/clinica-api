package br.com.clinica;

import static br.com.clinica.Models.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:tests;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
class ClinicaIntegrationTest {
  @TestConfiguration
  static class TimeConfig {
    @Bean
    @Primary
    Clock fixedClock() {
      return Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC);
    }
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate db;
  @Autowired ClinicaRepository repository;
  @Autowired AgendamentoService service;
  Pessoa paciente, profissional;
  final OffsetDateTime futuro = OffsetDateTime.parse("2030-02-01T12:00:00Z");

  @BeforeEach
  void limpar() {
    db.update("DELETE FROM agendamento");
    db.update("DELETE FROM paciente");
    db.update("DELETE FROM profissional");
    paciente = repository.cadastrar(false, "Ana");
    profissional = repository.cadastrar(true, "Dr. Paulo");
  }

  NovoAgendamento pedido(OffsetDateTime data) {
    return new NovoAgendamento(paciente.id(), profissional.id(), data, "Consulta");
  }

  String body(Object o) throws Exception {
    return json.writeValueAsString(o);
  }

  @Test
  void cadastraEListaPaciente() throws Exception {
    mvc.perform(
            post("/api/pacientes").contentType("application/json").content("{\"nome\":\"Maria\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty());
    mvc.perform(get("/api/pacientes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void rejeitaNomeEmBranco() throws Exception {
    mvc.perform(post("/api/pacientes").contentType("application/json").content("{\"nome\":\"  \"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void criaConsultaComStatusInicial() throws Exception {
    mvc.perform(
            post("/api/agendamentos").contentType("application/json").content(body(pedido(futuro))))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.status").value("AGENDADO"));
  }

  @Test
  void rejeitaPassadoEPresente() throws Exception {
    for (var data :
        List.of(
            OffsetDateTime.parse("2029-12-31T23:59:00Z"),
            OffsetDateTime.parse("2030-01-01T00:00:00Z")))
      mvc.perform(
              post("/api/agendamentos").contentType("application/json").content(body(pedido(data))))
          .andExpect(status().isBadRequest());
  }

  @Test
  void rejeitaDuplicidadeInclusiveFusosEquivalentes() throws Exception {
    service.criar(pedido(futuro));
    mvc.perform(
            post("/api/agendamentos")
                .contentType("application/json")
                .content(body(pedido(futuro.withOffsetSameInstant(ZoneOffset.ofHours(-3))))))
        .andExpect(status().isConflict());
  }

  @Test
  void permiteProfissionaisDiferentesNoMesmoHorario() {
    service.criar(pedido(futuro));
    var outro = repository.cadastrar(true, "Dra. Bia");
    assertThat(
            service
                .criar(new NovoAgendamento(paciente.id(), outro.id(), futuro, "Retorno"))
                .status())
        .isEqualTo(Status.AGENDADO);
  }

  @Test
  void cancelamentoMantemRegistroEMotivoELiberaHorario() throws Exception {
    var a = service.criar(pedido(futuro));
    mvc.perform(
            patch("/api/agendamentos/" + a.id() + "/cancelamento")
                .contentType("application/json")
                .content("{\"motivo\":\"Paciente solicitou\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELADO"))
        .andExpect(jsonPath("$.motivoCancelamento").value("Paciente solicitou"));
    service.criar(pedido(futuro));
    assertThat(repository.listar(null, null, null)).hasSize(2);
    assertThat(repository.buscar(a.id())).isPresent();
  }

  @Test
  void exigeMotivoENaoSobrescreveCancelamento() throws Exception {
    var a = service.criar(pedido(futuro));
    String url = "/api/agendamentos/" + a.id() + "/cancelamento";
    mvc.perform(patch(url).contentType("application/json").content("{\"motivo\":\" \"}"))
        .andExpect(status().isBadRequest());
    service.cancelar(a.id(), "Original");
    mvc.perform(patch(url).contentType("application/json").content("{\"motivo\":\"Outro\"}"))
        .andExpect(status().isConflict());
    assertThat(repository.buscar(a.id()).orElseThrow().motivoCancelamento()).isEqualTo("Original");
  }

  @Test
  void filtraIndividualmenteEEmConjunto() throws Exception {
    var a = service.criar(pedido(futuro));
    service.cancelar(a.id(), "Teste");
    var p2 = repository.cadastrar(false, "Bruno");
    var pro2 = repository.cadastrar(true, "Dra. Clara");
    service.criar(new NovoAgendamento(p2.id(), pro2.id(), futuro, "Retorno"));
    mvc.perform(get("/api/agendamentos").param("pacienteId", paciente.id().toString()))
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(get("/api/agendamentos").param("profissionalId", profissional.id().toString()))
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(get("/api/agendamentos").param("status", "AGENDADO"))
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(
            get("/api/agendamentos")
                .param("pacienteId", paciente.id().toString())
                .param("profissionalId", profissional.id().toString())
                .param("status", "CANCELADO"))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(a.id().toString()));
    mvc.perform(
            get("/api/agendamentos")
                .param("pacienteId", paciente.id().toString())
                .param("status", "AGENDADO"))
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void dadosInexistentesRetornam404() throws Exception {
    mvc.perform(
            post("/api/agendamentos")
                .contentType("application/json")
                .content(
                    body(
                        new NovoAgendamento(
                            UUID.randomUUID(), profissional.id(), futuro, "Consulta"))))
        .andExpect(status().isNotFound());
    mvc.perform(
            post("/api/agendamentos")
                .contentType("application/json")
                .content(
                    body(
                        new NovoAgendamento(paciente.id(), UUID.randomUUID(), futuro, "Consulta"))))
        .andExpect(status().isNotFound());
    mvc.perform(
            patch("/api/agendamentos/" + UUID.randomUUID() + "/cancelamento")
                .contentType("application/json")
                .content("{\"motivo\":\"Teste\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void rejeitaFormatoInvalidoECamposAusentes() throws Exception {
    mvc.perform(get("/api/agendamentos").param("status", "INVALIDO"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/agendamentos").param("pacienteId", "abc"))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/api/agendamentos").contentType("application/json").content("{}"))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/api/agendamentos").contentType("application/json").content("{errado"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/agendamentos")
                .contentType("application/json")
                .content(body(pedido(futuro.plusSeconds(1)))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void concorrenciaNaoProduzDuplaReserva() throws Exception {
    var executor = Executors.newFixedThreadPool(2);
    var inicio = new CountDownLatch(1);
    Callable<Integer> criar =
        () -> {
          inicio.await();
          try {
            service.criar(pedido(futuro));
            return 201;
          } catch (ApiException e) {
            return e.status.value();
          }
        };
    try {
      var a = executor.submit(criar);
      var b = executor.submit(criar);
      inicio.countDown();
      assertThat(List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS)))
          .containsExactlyInAnyOrder(201, 409);
      assertThat(repository.listar(null, null, null)).hasSize(1);
    } finally {
      executor.shutdownNow();
    }
  }
}
