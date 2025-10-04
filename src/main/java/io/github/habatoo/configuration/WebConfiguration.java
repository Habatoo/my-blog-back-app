package io.github.habatoo.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Конфигурация сервлета.
 *
 * <p>Основная конфигурация Spring MVC, которая включает поддержку веб-функциональности,
 * сканирование компонентов и загрузку свойств приложения.</p>
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "io.github.habatoo")
@PropertySource("classpath:application.properties")
public class WebConfiguration {
}
