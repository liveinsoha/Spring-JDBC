package hello.jdbc.service;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class MemberServiceV2Test {

    MemberServiceV2 memberServiceV2;

    @BeforeEach
    void beforeEach() throws SQLException {
        /*HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(URL);
        hikariDataSource.setUsername(USERNAME);
        hikariDataSource.setPassword(PASSWORD);*/
        //DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(URL);
        hikariDataSource.setUsername(USERNAME);
        hikariDataSource.setPassword(PASSWORD);
        MemberRepositoryV2 memberRepositoryV2 = new MemberRepositoryV2(hikariDataSource);
        memberServiceV2 = new MemberServiceV2(hikariDataSource, memberRepositoryV2);
    }

    @AfterEach
    void afterEach() throws SQLException {
        memberServiceV2.deleteAll();
    }

    @Test
    @DisplayName("커밋 테스트")
    void transferCommitTest() throws SQLException {

        Member memberA = new Member("aaa", 10000);
        Member memberB = new Member("bbb", 10000);

        memberServiceV2.join(memberA);
        memberServiceV2.join(memberB);

        memberServiceV2.transferMoney(memberA.getMemberId(), memberB.getMemberId(), 2000);

        Member fromMember = memberServiceV2.findById(memberA.getMemberId());
        Member toMember = memberServiceV2.findById(memberB.getMemberId());

        assertThat(fromMember.getMoney()).isEqualTo(8000);
        assertThat(toMember.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("롤백 테스트")
    void transferRollbackTest() throws SQLException {
        Member memberA = new Member("aaa", 10000);
        Member memberB = new Member("ex", 10000);

        memberServiceV2.join(memberA);
        memberServiceV2.join(memberB);

        assertThatThrownBy(() -> memberServiceV2.transferMoney(memberA.getMemberId(), memberB.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        Member fromMember = memberServiceV2.findById("aaa");
        Member toMember = memberServiceV2.findById("ex");

        assertThat(fromMember.getMoney()).isEqualTo(10000);
        assertThat(toMember.getMoney()).isEqualTo(10000);
    }

}