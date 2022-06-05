package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DBConnectionUtilTest {

    @Test
    void DBConnectionUtilTest() {

        // 우리가 맺어놓은 db 커넥션 받아옴
        Connection connection = DBConnectionUtil.getConnection();

        // 커넥션이 null이 아니면 success
        assertThat(connection).isNotNull();
    }
}
