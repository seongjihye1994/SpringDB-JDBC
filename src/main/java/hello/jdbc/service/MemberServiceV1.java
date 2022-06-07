package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

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
