package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials getDatabaseServiceCredentials() {
        String vcapServices = System.getenv("VCAP_SERVICES");
        return new DatabaseServiceCredentials(vcapServices);
    }

    @Bean
    public HibernateJpaVendorAdapter getHibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerMoviesFactory(@Qualifier("moviesDataSource")DataSource dataSourceMovies, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSourceMovies);
        factory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factory.setPackagesToScan("org.superbiz.moviefun");
        factory.setPersistenceUnitName("movies_unit");
        return factory;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerAlbumsFactory(@Qualifier("albumsDataSource") DataSource dataSourceAlbums, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSourceAlbums);
        factory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factory.setPackagesToScan("org.superbiz.moviefun");
        factory.setPersistenceUnitName("albums_unit");
        return factory;
    }

    @Bean
    public PlatformTransactionManager getMoviesPlatformTransactionManager(@Qualifier("entityManagerMoviesFactory")LocalContainerEntityManagerFactoryBean moviesEntityManagerFactoryBean){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(moviesEntityManagerFactoryBean.getObject());
        return jpaTransactionManager;
    }

    @Bean
    public PlatformTransactionManager getAlbumsPlatformTransactionManager(@Qualifier("entityManagerAlbumsFactory")LocalContainerEntityManagerFactoryBean albumsEntityManagerFactoryBean){
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(albumsEntityManagerFactoryBean.getObject());
        return jpaTransactionManager;
    }



    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql", "p-mysql"));
        return getHikariDataSource(dataSource);
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql", "p-mysql"));
        return getHikariDataSource(dataSource);
    }

    private HikariDataSource getHikariDataSource(MysqlDataSource dataSource) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSource(dataSource);
        return new HikariDataSource(hikariConfig);
    }
}
