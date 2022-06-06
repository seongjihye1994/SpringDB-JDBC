package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    // 기존 DriverManager 를 사용한 connection 획득
    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    // 스프링이 제공하는 DriverManagerDataSource 클래스 사용
    @Test
    void dataSourceDriverManager() throws SQLException {
        // DriverManagerDataSource - 얘도 항상 새로운 커넥션을 획득, DriverManager를 내부에서 직접 사용하기 때문
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);// 스프링이 제공

        useDataSource(dataSource);
    }

    // DataSource 를 통해 커넥션 풀을 사용
    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // 커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource(); // hikari pool 사용

        // hikari pool 설정
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10); // 디폴트가 10개
        dataSource.setPoolName("MyPool");

        useDataSource(dataSource);
        Thread.sleep(1000); // 커넥션 풀을 생성하는 로직은 별도의 쓰레드가 담당한다.
        // 그래서 별도의 쓰레드가 커넥션 풀을 생성하기 전에 테스트 코드가 끝나버릴 수 있기 때문에
        // 커넥션 풀 생성 로그를 확인하기 위해 테스트 쓰레드를 10초정도 sleep 시켜준다.
    }


    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

    /**
     * 스프링은 DriverManager 도 DataSource 를 통해서 사용할 수 있도록
     * DriverManagerDataSource 라는 DataSource 를 구현한 클래스를 제공
     */
}
