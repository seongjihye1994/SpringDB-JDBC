package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저 사용
 *
 * 트랜잭션 매니저는 dataSource 를 통해 커넥션을 만들고 트랜잭션을 시작한다.
 * -> 커넥션 조회… 오토커밋 false… 트랜잭션 매니저가 자동으로 해줍니당.
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    private final PlatformTransactionManager transactionManager; // 트랜잭션 매니저 주입!
    private final MemberRepositoryV3 memberRepository;

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

        // === 트랜잭션 시작 ===
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        /**
         * 트랜잭션 매니저는 dataSource 를 통해 커넥션을 만들고 트랜잭션을 시작한다.
         * -> 커넥션 조회… 오토커밋 false… 트랜잭션 매니저가 자동으로 해줍니당.
         */

        try {
            // ===== 비즈니스 로직 ===== //
            bizLogic(fromId, toId, money);

            transactionManager.commit(status); // 성공 시 커넥션 커밋!

        } catch (Exception e) {

            transactionManager.rollback(status); // 실패 시 커넥션 롤백!

            throw new IllegalStateException(e);
        } // release 는 트랜잭션 매니저가 해주니까 finally 할 필요 x

    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {

        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        // 돈 보내는 사람의 money 를 파라미터 money 만큼 차감 하고 update
        memberRepository.update(fromId, fromMember.getMoney() - money);

        // 테스트를 위한 오류 발생시키기
        validation(toMember);

        // 돈 받는 사람의 money 를 파라미터 money 만큼 더하고 update
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) { // 받는 사람의 id가 ex 라면 예외 발생
            throw new IllegalStateException("이체 중 예외 발생!");
        }
    }
}
