package enviosrutasdelinca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SeguridadConfiguracion {

    @Bean
    SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/**"))
            .authorizeHttpRequests(autorizacion -> autorizacion
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated())
                .formLogin(formulario -> formulario.disable())
                .httpBasic(basico -> basico.disable())
                .build();
    }
}