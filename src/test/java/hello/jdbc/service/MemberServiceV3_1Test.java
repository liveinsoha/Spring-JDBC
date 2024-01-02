package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class MemberServiceV3_1Test {

    MemberServiceV3_1 memberServiceV3;

    @BeforeEach
    void beforeEach() throws SQLException {

        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        /*HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(URL);
        hikariDataSource.setUsername(USERNAME);
        hikariDataSource.setPassword(PASSWORD);*/
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        MemberRepositoryV3 memberRepositoryV3 = new MemberRepositoryV3(dataSource);
        memberServiceV3 = new MemberServiceV3_1(transactionManager, memberRepositoryV3);
    }

    @AfterEach
    void afterEach() throws SQLException {
        memberServiceV3.deleteAll();
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