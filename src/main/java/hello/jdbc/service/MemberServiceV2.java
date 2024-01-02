package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepositoryV2;


    public void transferMoney(String fromId, String toId, int money) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            bizLogic(connection, fromId, toId, money);
            connection.commit();
            log.info("transfer account commit");
        } catch (IllegalStateException e) {
            /**
             * 이체중 예외가 발생하면 예외를 잡고 롤백처리를 한 후 다시 같은 예외를 던진다
             */
            connection.rollback();
            log.info("transfer account rollback");
            throw new IllegalStateException(e.getMessage());
        } finally {
            release(connection);
        }
    }

    public void deleteAll() throws SQLException {
        memberRepositoryV2.deleteAll();
    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepositoryV2.findById(con, fromId);
        Member toMember = memberRepositoryV2.findById(con, toId);

        memberRepositoryV2.update(con, fromId, fromMember.getMoney() - money);
        validate(toId);
        memberRepositoryV2.update(con, toId, fromMember.getMoney() + money);
    }

    private void release(Connection con) {
        try {
            con.setAutoCommit(true);
            con.close();
        } catch (SQLException e) {
            log.error("db error", e);
        }
    }

    public void join(Member member) throws SQLException {
        memberRepositoryV2.save(member);
    }

    public Member findById(String memberId) {
        return memberRepositoryV2.findById(memberId);
    }

    private void validate(String toId) {
        if (toId.equals("ex")) {
            throw new IllegalStateException("예외 발생");
        }
    }
}
