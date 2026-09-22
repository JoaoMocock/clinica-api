package br.com.clinica;
import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
@SpringBootApplication
public class ClinicaApplication {
 public static void main(String[] args) { SpringApplication.run(ClinicaApplication.class, args); }
 @Bean Clock clock() { return Clock.systemUTC(); }
}
