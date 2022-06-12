package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCatch(); // 1. 예외를 잡는 메소드 호출
    }

    @Test
    void unchecked_throw() {
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrow()) // 1. 예외를 던지는 메소드 호출
                .isInstanceOf(MyUncheckedException.class); // 6. 받은 예외를 확인한다.
    }

    /**
     * RuntimeException을 상속받은 예외는 언체크 예외(런타임 예외)가 된다.
     */
    static class MyUncheckedException extends RuntimeException {

        public MyUncheckedException(String message) {
            super(message);
        }
    }

    /**
     * Unchecked 예외는
     * 예외를 잡거나, 던지지 않아도 된다.
     * 예외를 잡지 않으면 자동으로 밖으로 던진다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 필요한 경우 예외를 잡아서 처리하면 된다.
         */
        public void callCatch() {
            try { // 5. 받은 예외를 잡는다.
                repository.call(); // 2. 메소드 호출
            } catch (MyUncheckedException e) {
                // 예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }

        /**
         * 예외를 잡지 않아도 된다.
         * 자연스럽게 상위로 넘어간다.
         * 체크 예외와 다르게, throws 예외 선언을 하지 않아도 된다.
         */
        public void callThrow() { // 5. 예외를 호출한 곳으로 또 던진다.
            repository.call(); // 2. 메소드 호출
        }
    }


    static class Repository {
        public void call() { // 4. try catch가 없다. -> 내부에서 잡지않고 호출한 곳으로 예외를 던진다.
            throw new MyUncheckedException("ex"); // 3. 예외를 터트린다.
        }
    }


}
