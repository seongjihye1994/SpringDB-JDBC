package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * 트랜잭션 -  @Transactional AOP
 */
@Slf4j
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_3(MemberRepositoryV3 memberRepository) {
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
    @Transactional // 스프링 제공 트랜잭션 AOP 사용 -> 트랜잭션 시작(커넥션, 커밋, 롤백 자동으로 해줌)
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
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
