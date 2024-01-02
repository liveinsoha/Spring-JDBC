package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV4_1;
import hello.jdbc.repository.MemberRepositoryV4_2;
import hello.jdbc.repository.MemberRepositoryV5;
import lombok.extern.slf4j.Slf4j;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Slf4j
class MemberServiceV4Test {

    @Autowired
    MemberServiceV4 memberServiceV4;
    @Autowired
    MemberRepository memberRepository;

    @TestConfiguration
    static class TestConfig {

        DataSource dataSource;

        /**
         * 데이터소스와 트랜잭션 매니저를 스프링 빈으로 등록하는 코드가 생략되었다. 따라서 스프링 부트가
         * `application.properties` 에 지정된 속성을 참고해서 데이터소스와 트랜잭션 매니저를 자동으로 생성해준다.
         * 코드에서 보는 것 처럼 생성자를 통해서 스프링 부트가 만들어준 데이터소스 빈을 주입 받을 수도 있다.
         */

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() {
            //return new MemberRepositoryV4_1(dataSource); //단순 예외 반환
            //return new MemberRepositoryV4_2(dataSource); //스프링 예외 반환
            return new MemberRepositoryV5(dataSource); // JdbcTemplate
        }

        @Bean
        MemberServiceV4 memberServiceV4() {
            return new MemberServiceV4(memberRepository());
        }
    }

    @AfterEach
    void afterEach() throws SQLException {
        memberServiceV4.deleteAll();
    }

    @Test
    void aopCheck() {
        log.info("memberServiceV4 = {}", memberServiceV4.getClass());
        log.info("memberRepository = {}", memberRepository.getClass());
        assertThat(AopUtils.isAopProxy(memberServiceV4)).isTrue();
        assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();

    }

    @Test
    @DisplayName("커밋 테스트")
    void transferCommitTest() throws SQLException {

        Member memberA = new Member("aaa", 10000);
        Member memberB = new Member("bbb", 10000);

        memberServiceV4.join(memberA);
        memberServiceV4.join(memberB);

        memberServiceV4.transferMoney(memberA.getMemberId(), memberB.getMemberId(), 2000);

        Member fromMember = memberServiceV4.findById(memberA.getMemberId());
        Member toMember = memberServiceV4.findById(memberB.getMemberId());

        assertThat(fromMember.getMoney()).isEqualTo(8000);
        assertThat(toMember.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("롤백 테스트")
    void transferRollbackTest() throws SQLException {
        Member memberA = new Member("aaa", 10000);
        Member memberB = new Member("ex", 10000);

        memberServiceV4.join(memberA);
        memberServiceV4.join(memberB);

        assertThatThrownBy(() -> memberServiceV4.transferMoney(memberA.getMemberId(), memberB.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        Member fromMember = memberServiceV4.findById("aaa");
        Member toMember = memberServiceV4.findById("ex");

        assertThat(fromMember.getMoney()).isEqualTo(10000);
        assertThat(toMember.getMoney()).isEqualTo(10000);
    }
}