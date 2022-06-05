package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    public static Connection getConnection() {

        try {
            /**
             * 데이터베이스에 연결하려면 JDBC가 제공하는 DriverManager.getConnection(..) 를 사용하면 된다.
             *
             * 이렇게 하면 드라이버 매너지가 라이브러리에 있는 데이터베이스 드라이버를 찾아서
             * 해당 드라이버가 제공하는 커넥션을 반환해준다.
             *
             * 여기서는 H2 데이터베이스 드라이버가 작동해서 실제 데이터베이스와 커넥션을 맺고 그
             * 결과를 반환해준다.
             *
             * h2 db 드라이버는 우리가 스프링 이니셜라이저에서 디펜던시로 라이브러리를 넣어줬다.
             */
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection={}, class={}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException(e); // checked exception 을 runtime exception 으로 바꿔서 예외를 던짐
        }
    }
}
