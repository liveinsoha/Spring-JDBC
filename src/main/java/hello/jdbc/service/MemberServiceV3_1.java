package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;

@RequiredArgsConstructor
@Slf4j
public class MemberServiceV3_1 {

    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepositoryV3;


    public void transferMoney(String fromId, String toId, int money) throws SQLException {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {

            bizLogic(fromId, toId, money);
            transactionManager.commit(status);
            log.info("transactionManager commit");
        } catch (IllegalStateException e) {
            /**
             * 이체중 예외가 발생하면 예외를 잡고 롤백처리를 한 후 다시 같은 예외를 던진다
             */
            transactionManager.rollback(status);
            log.info("transactionManager rollback");
            throw new IllegalStateException(e.getMessage());
        }
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
