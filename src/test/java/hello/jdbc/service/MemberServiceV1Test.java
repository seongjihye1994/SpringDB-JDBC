package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 기본 동작, 트랜잭션이 없어서 문제가 발생하는 테스트
 */
class MemberServiceV1Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV1 memberRepository;
    private MemberServiceV1 memberService;

    // 각 테스트 시작 전 수행되는 메소드!! - 테스트 전 초기 세팅
    @BeforeEach
    void before() {

        // DB 커넥션을 스프링이 제공하는 DriverManagerDataSource 사용
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // 의존성 주입
        memberRepository = new MemberRepositoryV1(dataSource);
        memberService = new MemberServiceV1(memberRepository);
    }

    // 각 테스트 수행 후 호출되는 메소드!! - 테스트 후 정리
    @AfterEach
    void after() throws SQLException {

        // 테스트시 db에 member가 생성되어 테스트를 다시 할때마다 db를 다시 초기화 해줘야 하는 번거로움이 있다.
        // 이를 AfterEach 메소드를 호출하여 각 테스트 메소드 실행 후 호출되도록 하여 자동으로 db 데이터를 지워준다.

        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);

    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {

        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000); // memberA : fromId -> 8000원
        assertThat(findMemberB.getMoney()).isEqualTo(12000); // memberB : toId -> 12000원

    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void accountTransferEx() throws SQLException {

        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEX = new Member(MEMBER_EX, 10000); // memberId 가 EX 면 예외 발생!

        memberRepository.save(memberA);
        memberRepository.save(memberEX);

        // when -> memberService.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000) 의 수행 결과가 IllegalStateException 이 터져야 정상
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEX.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000); // memberA : fromId -> 8000원
        assertThat(findMemberB.getMoney()).isEqualTo(10000); // memberB : toId -> 10000원

        // 현재 트랜잭션 관련된 것이 하나도 없기 때문에
        // auto commit 모드이다.
        // 그래서 한 줄씩 모두 자동으로 커밋 된다.
        // 근대 중간에 에러가 발생했다.
        // 그래서 A의 돈은 8000원이고, EX의 돈은 10000원이 되어야 한다.
    }

}