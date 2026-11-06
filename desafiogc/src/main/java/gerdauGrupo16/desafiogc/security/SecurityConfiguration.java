package gerdauGrupo16.desafiogc.security;

/*
 * ARQUIVO: SecurityConfiguration.java
 * TIPO: classe de configuração (@Configuration, @EnableWebSecurity, @EnableMethodSecurity).
 * FUNÇÃO: define quem pode acessar o quê e como é o login. As regras de URL são avaliadas na ordem e vale a
 *   primeira que casar: arquivos estáticos liberados; /admin e /vinculos só ADMIN; /fornecedor só ADMIN;
 *   POST /api/servicos e /servicos/novo só COMPRADOR e FORNECEDOR (o administrador não solicita serviço); o
 *   resto exige login. Login por formulário em /login e logout em /logout. O CSRF fica ligado nas páginas e
 *   desligado em /api para testar no Swagger (remover antes de produção).
 * MÉTODOS:
 * - securityFilterChain(http): monta a cadeia de segurança com as regras acima.
 * - passwordEncoder(): codificador BCrypt usado para guardar e conferir senhas.
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/vinculos/**").hasRole("ADMIN")

                .requestMatchers("/fornecedor/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/servicos").hasAnyRole("COMPRADOR", "FORNECEDOR")
                .requestMatchers("/servicos/novo").hasAnyRole("COMPRADOR", "FORNECEDOR")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/servicos", true)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll())

            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
