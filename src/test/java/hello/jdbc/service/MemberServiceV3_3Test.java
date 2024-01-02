package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Slf4j
class MemberServiceV3_3Test {

    @Autowired
    MemberServiceV3_3 memberServiceV3;
    @Autowired
    MemberRepositoryV3 memberRepositoryV3;

    @TestConfiguration
    static class TestConfig {

        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource());
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }

    @AfterEach
    void afterEach() throws SQLException {
        memberServiceV3.deleteAll();
    }

    @Test
    void aopCheck() {

        log.info("memberServiceV3 = {}", memberServiceV3.getClass());
        log.info("memberRepositoryV3 = {}", memberRepositoryV3.getClass());
        assertThat(AopUtils.isAopProxy(memberServiceV3)).isTrue();
        assertThat(AopUtils.isAopProxy(memberRepositoryV3)).isFalse();

    }

    @Test
    @DisplayName("커밋 테스트")
    void transferCommitTest() throws SQLException {

        Member memberA = new Member("aaa", 10000);
        Member memberB = new Member("bbb", 10000);

        memberServiceV3.join(memberA);
        memberServiceV3.join(memberB);

        memberServiceV3.transferMoney(memberA.getMemberId(), memberB.getMemberId(), 2000);

        Member fromMember = memberServiceV3.findById(memberA.getMemberId());
        Member toMember = memberServiceV3.findById(memberB.getMemberId());

        assertThat(fromMember.getMoney()).isEqualTo(8000);
        assertThat(toMember.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("롤백 테스트")
    void transferRollbackTest() throws SQLException {
        Member memberA = new Member("aaa", 10000);
        Member memberB = new Member("ex", 10000);

        memberServiceV3.join(memberA);
        memberServiceV3.join(memberB);

        assertThatThrownBy(() -> memberServiceV3.transferMoney(memberA.getMemberId(), memberB.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        Member fromMember = memberServiceV3.findById("aaa");
        Member toMember = memberServiceV3.findById("ex");

        assertThat(fromMember.getMoney()).isEqualTo(10000);
        assertThat(toMember.getMoney()).isEqualTo(10000);
    }
}