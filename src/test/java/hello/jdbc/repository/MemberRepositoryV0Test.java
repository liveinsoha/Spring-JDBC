package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 memberRepositoryV0 = new MemberRepositoryV0();


    @Test
    void test() throws SQLException {
        Member member = new Member("aaa", 1000);
        memberRepositoryV0.save(member);

        Member findMember = memberRepositoryV0.findById("aaa");
        log.info("findMember = {}", findMember);
        assertThat(findMember.getMemberId()).isEqualTo("aaa");

        memberRepositoryV0.update("aaa", 2000);
        Member aaa = memberRepositoryV0.findById("aaa");
        assertThat(aaa.getMoney()).isEqualTo(2000);
    }

    @Test
    void deleteTest() throws SQLException {
        Member member = new Member("aaa", 1000);
        memberRepositoryV0.save(member);

        memberRepositoryV0.delete("aaa");

        assertThatThrownBy(() -> memberRepositoryV0.findById("aaa"))
                .isInstanceOf(NoSuchElementException.class);

    }


}