package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;


@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_3 {


    private final MemberRepositoryV3 memberRepositoryV3;

    /**
     * 순수한 비즈니스 로직만 남기고, 트랜잭션 관련 코드는 모두 제거했다.
     * 스프링이 제공하는 트랜잭션 AOP를 적용하기 위해 `@Transactional` 애노테이션을 추가했다.
     * `@Transactional` 애노테이션은 메서드에 붙여도 되고, 클래스에 붙여도 된다. 클래스에 붙이면 외부에서 호출
     * 가능한 `public` 메서드가 AOP 적용 대상이 된다
     */

    @Transactional
    public void transferMoney(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
    }

    public void deleteAll() throws SQLException {
        memberRepositoryV3.deleteAll();
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepositoryV3.findById(fromId);
        Member toMember = memberRepositoryV3.findById(toId);

        memberRepositoryV3.update(fromId, fromMember.getMoney() - money);
        validate(toId);
        memberRepositoryV3.update(toId, toMember.getMoney() + money);
    }


    public void join(Member member) throws SQLException {
        memberRepositoryV3.save(member);
    }

    public Member findById(String memberId) {
        return memberRepositoryV3.findById(memberId);
    }

    private void validate(String toId) {
        if (toId.equals("ex")) {
            throw new IllegalStateException("예외 발생");
        }
    }
}
