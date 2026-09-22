package br.com.clinica;
import static br.com.clinica.Models.*;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class ClinicaRepository {
 private final JdbcTemplate db;
 public ClinicaRepository(JdbcTemplate db) { this.db=db; }
 // Table names are selected internally, never supplied by an HTTP client.
 public Pessoa cadastrar(boolean profissional, String nome) {
   var pessoa=new Pessoa(UUID.randomUUID(),nome.strip());
   db.update("INSERT INTO "+(profissional ? "profissional" : "paciente")+" (id,nome) VALUES (?,?)",pessoa.id().toString(),pessoa.nome());
   return pessoa;
 }
 public List<Pessoa> pessoas(boolean profissional) {
   return db.query("SELECT id,nome FROM "+(profissional ? "profissional" : "paciente")+" ORDER BY nome,id",
     (r,n)->new Pessoa(UUID.fromString(r.getString("id")),r.getString("nome")));
 }
 public boolean existe(boolean profissional, UUID id) {
   return db.queryForObject("SELECT COUNT(*) FROM "+(profissional ? "profissional" : "paciente")+" WHERE id=?",Integer.class,id.toString())>0;
 }
 public void inserir(Agendamento a) {
   // Unique nullable key prevents double booking even across concurrent transactions.
   String chave=a.profissionalId()+"|"+a.dataHora().toInstant();
   db.update("INSERT INTO agendamento (id,paciente_id,profissional_id,data_hora,tipo_atendimento,status,horario_ativo) VALUES (?,?,?,?,?,?,?)",
      a.id().toString(),a.pacienteId().toString(),a.profissionalId().toString(),a.dataHora(),a.tipoAtendimento(),a.status().name(),chave);
 }
 public Optional<Agendamento> buscar(UUID id) {
   return db.query("SELECT * FROM agendamento WHERE id=?",this::mapear,id.toString()).stream().findFirst();
 }
 public List<Agendamento> listar(UUID paciente, UUID profissional, Status status) {
   String sql="SELECT * FROM agendamento WHERE 1=1"; var args=new ArrayList<Object>();
   if(paciente!=null) {sql+=" AND paciente_id=?";args.add(paciente.toString());}
   if(profissional!=null) {sql+=" AND profissional_id=?";args.add(profissional.toString());}
   if(status!=null) {sql+=" AND status=?";args.add(status.name());}
   return db.query(sql+" ORDER BY data_hora,id",this::mapear,args.toArray());
 }
 public boolean cancelar(UUID id,String motivo) {
   return db.update("UPDATE agendamento SET status='CANCELADO',motivo_cancelamento=?,horario_ativo=NULL WHERE id=? AND status='AGENDADO'",motivo,id.toString())==1;
 }
 private Agendamento mapear(ResultSet r,int n) throws SQLException {
   return new Agendamento(UUID.fromString(r.getString("id")),UUID.fromString(r.getString("paciente_id")),
     UUID.fromString(r.getString("profissional_id")),r.getObject("data_hora",OffsetDateTime.class),
     r.getString("tipo_atendimento"),Status.valueOf(r.getString("status")),r.getString("motivo_cancelamento"));
 }
}
