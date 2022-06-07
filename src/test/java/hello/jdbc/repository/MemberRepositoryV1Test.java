package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach // 각 테스트가 실행되기 직전에 한 번 호출됨
    void beforeEach() {
        // 기본 DriverManager - 항상 새로운 커넥션을 획득
        // DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // hikari 커넥션 풀링 사용
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        // MemberRepositoryV1 를 생생할 때 인자로 dataSource 넘김
        repository = new MemberRepositoryV1(dataSource);

    }

    @Test
    void crud() throws SQLException, InterruptedException {

        // save
        Member member = new Member("memberV0", 10000);

        repository.save(member);

        // find by id
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember = {}", findMember);

        assertThat(findMember).isEqualTo(member); // 검증

        // update : money를 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updateMember = repository.findById(member.getMemberId());

        assertThat(updateMember.getMoney()).isEqualTo(20000);

        // delete
        repository.delete(member.getMemberId());

        // 지운 회원은 디비에서 가져올 수 없다.
        // 그래서 지워지지 않으면 NoSuchElementException 예외가
        // 터지도록 로직을 작성했으니 지운 멤버의 아이디를 조회했을 때
        // NoSuchElementException 이 터지면 테스트 성공
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);

        Thread.sleep(1000);

    }


}
