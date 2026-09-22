package br.com.clinica;
import java.time.Instant;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
@RestControllerAdvice
public class ApiErrorHandler {
 private ResponseEntity<?> erro(HttpStatus status,String mensagem) {
   return ResponseEntity.status(status).body(Map.of("status",status.value(),"mensagem",mensagem,"timestamp",Instant.now().toString()));
 }
 @ExceptionHandler(ApiException.class) ResponseEntity<?> negocio(ApiException e) {return erro(e.status,e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validacao(MethodArgumentNotValidException e) {
   String mensagem=e.getBindingResult().getFieldErrors().stream().map(f->f.getField()+": "+f.getDefaultMessage()).sorted().collect(java.util.stream.Collectors.joining("; "));
   return erro(HttpStatus.BAD_REQUEST,mensagem);
 }
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class})
 ResponseEntity<?> formato(Exception e) {return erro(HttpStatus.BAD_REQUEST,"JSON, identificador, status ou data/hora inválidos. Use ISO 8601 com fuso horário.");}
 @ExceptionHandler(NoResourceFoundException.class) ResponseEntity<?> ausente(Exception e) {return erro(HttpStatus.NOT_FOUND,"Recurso não encontrado.");}
 @ExceptionHandler(HttpRequestMethodNotSupportedException.class) ResponseEntity<?> metodo(Exception e) {return erro(HttpStatus.METHOD_NOT_ALLOWED,"Método não permitido.");}
 @ExceptionHandler(Exception.class) ResponseEntity<?> inesperado(Exception e) {
   LoggerFactory.getLogger(getClass()).error("Falha não tratada",e);
   return erro(HttpStatus.INTERNAL_SERVER_ERROR,"Erro interno ao processar a solicitação.");
 }
}
