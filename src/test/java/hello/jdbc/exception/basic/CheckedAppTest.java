package hello.jdbc.exception.basic;

import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CheckedAppTest {

    @Test
    void checked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
            .isInstanceOf(Exception.class);
    }                   

    static class Controller {
        Service service = new Service();

        // SQL 오류, 네트워크 커넥트 오류는 컨트롤러 단에서 처리할 수 없다... 예외를 던진다.
        public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        // SQL 오류, 네트워크 커넥트 오류는 서비스 단에서 처리할 수 없다... 예외를 던진다.
        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient {
        public void call() throws ConnectException {
            throw new ConnectException("연결 실패"); // 체크 예외
        }
    }

    static class Repository {
        public void call() throws SQLException {
            throw new SQLException("ex"); // 체크 예외
        }
    }

}
