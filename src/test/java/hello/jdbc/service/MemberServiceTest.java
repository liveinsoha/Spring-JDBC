package hello.jdbc.service;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class MemberServiceTest {

    MemberService memberService;

    @BeforeEach
    void beforeEach() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(URL);
        hikariDataSource.setUsername(USERNAME);
        hikariDataSource.setPassword(PASSWORD);

        MemberRepositoryV1 memberRepositoryV1 = new MemberRepositoryV1(hikariDataSource);
        memberService = new MemberService(memberRepositoryV1);
    }

    @Test
    void transferOKTest() throws SQLException {

        Member memberA = new Member("aaa", 10000);
        Member memberB = new Member("bbb", 10000);

        memberService.join(memberA);
        memberService.join(memberB);

        memberService.transferMoney(memberA.getMemberId(), memberB.getMemberId(), 2000);

        Member fromMember = memberService.findById(memberA.getMemberId());
        Member toMember = memberService.findById(memberB.getMemberId());

        assertThat(fromMember.getMoney()).isEqualTo(8000);
        assertThat(toMember.getMoney()).isEqualTo(12000);
    }

    @Test
    void transferFailTest() throws SQLException {

        Member memberA = new Member("aaa", 10000);
        Member memberB = new Member("ex", 10000);

        memberService.join(memberA);
        memberService.join(memberB);

        assertThatThrownBy(() -> memberService.transferMoney(memberA.getMemberId(), memberB.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        Member fromMember = memberService.findById(memberA.getMemberId());
        Member toMember = memberService.findById(memberB.getMemberId());

        assertThat(fromMember.getMoney()).isEqualTo(8000);
        assertThat(toMember.getMoney()).isEqualTo(10000);
    }

}