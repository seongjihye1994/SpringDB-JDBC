package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 탬플릿 사용
 *
 * 트랜잭션 매니저는 dataSource 를 통해 커넥션을 만들고 트랜잭션을 시작한다.
 * -> 커넥션 조회… 오토커밋 false… 트랜잭션 매니저가 자동으로 해줍니당.
 */
@Slf4j
public class MemberServiceV3_2 {

    // private final PlatformTransactionManager transactionManager; // 트랜잭션 매니저 주입!
    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager); // 트랜잭션 탬플릿을 사용하려면, 트랜잭션 매니저가 필요하다.
        this.memberRepository = memberRepository;
    }

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

        // 트랜잭션 탬플릿 사용!
        // accountTransfer 메소드가 void 이기 때문에 리턴값이 없을 때 사용하는 executeWithoutResult 사용
        txTemplate.executeWithoutResult(status -> { // 2. executeWithoutResult 에서 비즈니스 로직 결과가 성공이면 커밋, 실패면 롤백
            // ===== 비즈니스 로직 ===== //
            try {
                bizLogic(fromId, toId, money); // 1. 이 비즈니스 로직이 수행되고
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
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
