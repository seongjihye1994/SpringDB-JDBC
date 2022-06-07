package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 사용 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    /**
     * 계좌이체 시나리오
     *
     * fromId : 돈 보내는 사람
     * toId : 돈 받는 사람
     *
     * @param fromId
     * @param toId
     * @param money
     * @throws SQLException
     */
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        Connection con = dataSource.getConnection();

        try {
            con.setAutoCommit(false); // 자동 커밋을 false -> 트랜잭션 시작!!

            // ===== 비즈니스 로직 ===== //
            bizLogic(con, fromId, toId, money);

            con.commit(); // 성공 시 커넥션 커밋!

        } catch (Exception e) {
            con.rollback(); // 실패 시 커넥션 롤백!
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }

    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        // 돈 보내는 사람의 money 를 파라미터 money 만큼 차감 하고 update
        memberRepository.update(con, fromId, fromMember.getMoney() - money);

        // 테스트를 위한 오류 발생시키기
        validation(toMember);

        // 돈 받는 사람의 money 를 파라미터 money 만큼 더하고 update
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) { // 받는 사람의 id가 ex 라면 예외 발생
            throw new IllegalStateException("이체 중 예외 발생!");
        }
    }

    private void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true); // 풀에 반납 하기 전 먼저 오토 커밋을 true 로 적용하고
                con.close(); // 풀에 반납해야 한다.
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }
}
