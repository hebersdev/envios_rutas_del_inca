package enviosrutasdelinca.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SeguridadConfiguracion {

    @Bean
    SecurityFilterChain filtroSeguridad(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            CookieCsrfTokenRepository csrfTokenRepository) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/auth/**"))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/api/auth/**", "/api/csrf").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/agencias/activas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cotizaciones/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/seguimiento/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/mis-envios").hasRole("CLIENTE")
                        .requestMatchers("/api/mis-envios/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/reportes/excel").hasRole("ENCARGADO_LOGISTICA")
                        .requestMatchers("/api/reportes/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/entregas/por-entregar")
                        .hasRole("COUNTER")
                        .requestMatchers(HttpMethod.GET, "/api/entregas/reparto", "/api/entregas/motorizados")
                        .hasRole("ENCARGADO_LOGISTICA")
                        .requestMatchers(HttpMethod.GET, "/api/entregas/mis-entregas").hasRole("MOTORIZADO")
                        .requestMatchers(HttpMethod.POST, "/api/entregas/listo-recojo", "/api/entregas/*/entregar-en-agencia")
                        .hasRole("COUNTER")
                        .requestMatchers(HttpMethod.POST, "/api/entregas/asignar-motorizado").hasRole("ENCARGADO_LOGISTICA")
                        .requestMatchers(HttpMethod.POST, "/api/entregas/salir-a-reparto", "/api/entregas/*/entregar")
                        .hasRole("MOTORIZADO")
                        .requestMatchers("/api/entregas/**").denyAll()
                        .requestMatchers("/api/viajes", "/api/viajes/**")
                        .hasRole("ENCARGADO_LOGISTICA")
                        .requestMatchers("/api/clientes/**").hasRole("COUNTER")
                        .requestMatchers(HttpMethod.POST, "/api/envios").hasRole("COUNTER")
                        .requestMatchers(HttpMethod.PATCH, "/api/envios/*/estado").hasRole("COUNTER")
                        .requestMatchers(HttpMethod.GET, "/api/envios", "/api/envios/**")
                        .hasAnyRole("COUNTER", "ADMINISTRADOR")
                        .requestMatchers("/api/tarifas", "/api/tarifas/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/camiones", "/api/camiones/**")
                        .hasAnyRole("ADMINISTRADOR", "ENCARGADO_LOGISTICA")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/agencias", "/api/agencias/**")
                        .hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated())
                .formLogin(formulario -> formulario.disable())
                .httpBasic(basico -> basico.disable())
                .build();
    }

    @Bean
    CookieCsrfTokenRepository csrfTokenRepository(
            @Value("${server.servlet.session.cookie.same-site:lax}") String sameSite,
            @Value("${server.servlet.session.cookie.secure:false}") boolean secure) {
        CookieCsrfTokenRepository repositorio =
                CookieCsrfTokenRepository.withHttpOnlyFalse();
        repositorio.setCookieCustomizer(cookie -> cookie
                .sameSite(sameSite)
                .secure(secure));
        return repositorio;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList());
        configuracion.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(
                List.of("Content-Type", "X-XSRF-TOKEN", "X-Requested-With"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource origen =
                new UrlBasedCorsConfigurationSource();
        origen.registerCorsConfiguration("/**", configuracion);
        return origen;
    }
}
