package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import hello.jdbc.repository.MemberRepositoryV4_1;
import hello.jdbc.repository.MemberRepositoryV4_2;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository 인터페이스 의존
 * */
@Slf4j
// 스프링 aop 사용하려면 스프링 컨테이너가 필요하다.
// 이 어노테이션이 있으면 테스트시 스프링 부트를 통해 스프링 컨테이너를 생성한다.
// 스프링 컨테이너 생성 시 의존관계를 주입해준다.
@SpringBootTest
class MemberServiceV4Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberServiceV4 memberService;

    // 테스트 환경에서 필요한 빈들을 등록할 수 있도록 도와주는 어노테이션
    // 스프링 부트를 통해 스프링 컨테이너 생성 시 자동으로 주입된 의존관계 외에,
    // 내가 설정한 빈(수동 빈)을 추가로 사용하고 싶을 때
    @TestConfiguration
    static class TestConfig {

        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() {
//            return new MemberRepositoryV4_1(dataSource);
            return new MemberRepositoryV4_2(dataSource);
        }


        @Bean
        MemberServiceV4 memberServiceV4() {
            return new MemberServiceV4(memberRepository());
        }

    }


    // 각 테스트 수행 후 호출되는 메소드!! - 테스트 후 정리
    @AfterEach
    void after() {

        // 테스트시 db에 member가 생성되어 테스트를 다시 할때마다 db를 다시 초기화 해줘야 하는 번거로움이 있다.
        // 이를 AfterEach 메소드를 호출하여 각 테스트 메소드 실행 후 호출되도록 하여 자동으로 db 데이터를 지워준다.

        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    void AopCheck() {
        log.info("memberService class={}", memberService.getClass());
        log.info("memberRepository class={}", memberRepository.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue(); // memberService가 프록시냐?
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse(); // memberRepository가 프록시가 아니냐?
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() {

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
    void accountTransferEx() {

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

        assertThat(findMemberA.getMoney()).isEqualTo(10000); // memberA : fromId -> 8000원
        assertThat(findMemberB.getMoney()).isEqualTo(10000); // memberB : toId -> 10000원

        // 이 코드에서는 트랜잭션이 적용되었기 때문에 memberA의 돈이 8000으로 테스트되면
        // 예외가 발생한다. - memberA의 돈도 10000으로 수정해야 한다. 롤백되었기 때문
    }

}