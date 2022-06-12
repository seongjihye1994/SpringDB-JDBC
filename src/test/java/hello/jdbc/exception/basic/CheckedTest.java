package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class CheckedTest {

    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_Throw() {
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyCheckedException.class);
    }

    /**
     * Exception을 상속받은 예외는 체크 예외(컴파일 예외)가 된다.
     */
    static class MyCheckedException extends Exception {

        public MyCheckedException(String message) {
            super(message);
        }
    }

    /**
     * Checked 예외는
     * 예외를 잡아서 처리하거나, 던지거나 둘 중 하나를 필수로 선택해야 한다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         */
        public void callCatch() {
            try {
                repository.call(); // Repository 에서 던진 예외가 호출한 곳으로 넘어왔다. 역시 내부에서 잡거나, Service를 호출하는 곳으로 던져야 한다.
            } catch (MyCheckedException e) {
                // 예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }

        /**
         * 체크 예외를 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 메서드에 필수로 선언해야 한다.
         * @throws MyCheckedException
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository {
        public void call() throws MyCheckedException { // throws MyCheckedException : 예외를 호출한 상위 클래스로 던진다.
            throw new MyCheckedException("ex"); // 예외 터트림

            // throws MyCheckedException 를 해주지 않으면 컴파일 예외가 떨어짐.
            // 예외를 처리하는 방법
            // 1. throws MyCheckedException 처럼 호출한 상위 메소드로 예외를 던진다.
            // 2. try catch 로 예외를 자신 메소드에서 처리한다.

        }
    }


}
