package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,
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
    public DatabaseServiceCredentials databaseServiceCredentials(@Value("${VCAP_SERVICES}") String VCAP_SERVICES) {

       return new DatabaseServiceCredentials(VCAP_SERVICES);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials databaseServiceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(databaseServiceCredentials.jdbcUrl("albums-mysql"));
        HikariDataSource hikariDataSource=new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }


    @Bean
    public DataSource movieDataSource(DatabaseServiceCredentials databaseServiceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(databaseServiceCredentials.jdbcUrl("movies-mysql"));
        HikariDataSource hikariDataSource=new HikariDataSource();
        hikariDataSource.setDataSource(dataSource);
        return hikariDataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter(){

        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getMovieEntityManagerBean(DataSource movieDataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {

        LocalContainerEntityManagerFactoryBean movieentityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        movieentityManagerFactoryBean.setDataSource(movieDataSource);
        movieentityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        movieentityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.movies");
        movieentityManagerFactoryBean.setPersistenceUnitName("movie-unit");
        return movieentityManagerFactoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getAlbumEntityManagerBean(DataSource albumsDataSource, HibernateJpaVendorAdapter hva) {

        LocalContainerEntityManagerFactoryBean albumEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        albumEntityManagerFactoryBean.setDataSource(albumsDataSource);
        albumEntityManagerFactoryBean.setJpaVendorAdapter(hva);
        albumEntityManagerFactoryBean.setPackagesToScan("org.superbiz.moviefun.albums");
        albumEntityManagerFactoryBean.setPersistenceUnitName("album-unit");
        return albumEntityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager movieTransactionManager(EntityManagerFactory getMovieEntityManagerBean){
        JpaTransactionManager transactionManager= new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(getMovieEntityManagerBean );
        return transactionManager;
    }

    @Bean
    public PlatformTransactionManager albumTransactionManager(EntityManagerFactory getAlbumEntityManagerBean){
        JpaTransactionManager transactionManager= new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(getAlbumEntityManagerBean );
        return transactionManager;
    }
}
