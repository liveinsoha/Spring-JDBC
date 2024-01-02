package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;


@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate transactionTemplate;
    private final MemberRepositoryV3 memberRepositoryV3;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepositoryV3) {
        /**
         * `TransactionTemplate` 을 사용하려면 `transactionManager` 가 필요하다. 생성자에서
         * `transactionManager` 를 주입 받으면서 `TransactionTemplate` 을 생성했다.
         */
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.memberRepositoryV3 = memberRepositoryV3;
    }

    public void transferMoney(String fromId, String toId, int money) throws SQLException {
        transactionTemplate.executeWithoutResult((status) -> {
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException();
            }
        });


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
