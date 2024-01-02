package hello.jdbc.exception;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Slf4j
public class UncheckedAppTest {

    @Test
    void UncheckedException() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request()).isInstanceOf(Exception.class);
    }

    @Test
    void printEx() {

        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
            log.error("Error 발생", e);
        }
    }

    static class Controller {
        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient {
        public void call() {
            throw new RuntimeNetworkException("네트워크 예외");
        }
    }

    static class Repository {

        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSQLException();
            }
        }

        private void runSQL() throws SQLException {
            throw new SQLException("메모리 예외");
        }

    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException() {
        }

        public RuntimeSQLException(String message) {
            super(message);
        }

        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }

    static class RuntimeNetworkException extends RuntimeException {

        public RuntimeNetworkException(String message) {
            super(message);
        }
    }
}
