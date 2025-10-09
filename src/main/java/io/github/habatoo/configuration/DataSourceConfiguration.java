package io.github.habatoo.configuration;

import org.h2.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Конфигурация компонентов, которые относятся к базе данных.
 *
 * <p>Содержит настройки для подключения к базе данных, создания необходимых
 * компонентов Spring JDBC и инициализации схемы данных.</p>
 */
@Configuration
public class DataSourceConfiguration {

    /**
     * Настройка DataSource — компонент, отвечающий за соединение с базой данных.
     *
     * @param url      адрес БД
     * @param username имя пользователя БД
     * @param password пароль
     * @return DataSource соединения с БД
     */
    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password
    ) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    /**
     * Компонент JdbcTemplate для выполнения запросов.
     *
     * @param dataSource объект соединения с БД
     * @return объект шаблона JdbcTemplate
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Инициализация схемы БД до создания бинов, чтобы таблицы были доступны.
     */
    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(new ClassPathResource("schema.sql"));
        databasePopulator.setContinueOnError(false);

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator);
        initializer.setEnabled(true);
        return initializer;
    }

//    /**
//     * Метод наполнения БД после инициализации контекста.
//     *
//     * @param event Событие инициализации контекста
//     */
//    @EventListener
//    public void populate(ContextRefreshedEvent event) {
//        DataSource dataSource = event.getApplicationContext().getBean(DataSource.class);
//
//        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
//        populator.addScript(new ClassPathResource("schema.sql"));
//        populator.execute(dataSource);
//    }
}
