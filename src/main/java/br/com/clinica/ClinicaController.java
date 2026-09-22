package br.com.clinica;
import static br.com.clinica.Models.*;
import java.net.URI;
import java.util.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
public class ClinicaController {
 private final ClinicaRepository repository;
 private final AgendamentoService service;
 public ClinicaController(ClinicaRepository repository,AgendamentoService service) {this.repository=repository;this.service=service;}
 @PostMapping("/pacientes") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
 public Pessoa paciente(@Valid @RequestBody Cadastro body) {return repository.cadastrar(false,body.nome());}
 @GetMapping("/pacientes") public List<Pessoa> pacientes() {return repository.pessoas(false);}
 @PostMapping("/profissionais") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
 public Pessoa profissional(@Valid @RequestBody Cadastro body) {return repository.cadastrar(true,body.nome());}
 @GetMapping("/profissionais") public List<Pessoa> profissionais() {return repository.pessoas(true);}
 @PostMapping("/agendamentos") public ResponseEntity<Agendamento> criar(@Valid @RequestBody NovoAgendamento body) {
   var a=service.criar(body);return ResponseEntity.created(URI.create("/api/agendamentos/"+a.id())).body(a);
 }
 @GetMapping("/agendamentos/{id}") public Agendamento buscar(@PathVariable UUID id) {
   return repository.buscar(id).orElseThrow(()->new ApiException(org.springframework.http.HttpStatus.NOT_FOUND,"Agendamento não encontrado."));
 }
 @GetMapping("/agendamentos") public List<Agendamento> listar(@RequestParam(required=false) UUID pacienteId,
   @RequestParam(required=false) UUID profissionalId,@RequestParam(required=false) Status status) {
   return repository.listar(pacienteId,profissionalId,status);
 }
 @PatchMapping("/agendamentos/{id}/cancelamento") public Agendamento cancelar(@PathVariable UUID id,@Valid @RequestBody Cancelamento body) {
   return service.cancelar(id,body.motivo());
 }
}
