package gerdauGrupo16.desafiogc.security;

/*
 * ARQUIVO: WebConfiguration.java
 * TIPO: classe de configuração do Spring MVC (@Configuration, WebMvcConfigurer).
 * FUNÇÃO: registra rotas que só mostram uma página, sem controller.
 * MÉTODOS:
 * - addViewControllers(registry): liga /login à página servicos/login, com prioridade máxima.
 */

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("servicos/login");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
