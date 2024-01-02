package hello.jdbc.exception.translator;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.exception.MyDBException;
import hello.jdbc.exception.MyDuplicateKeyException;
import hello.jdbc.service.MemberService;
import hello.jdbc.service.MemberServiceV4;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Random;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ExTranslatorV1Test {
    /**
     * e.getErrorCode() == 23505` : 오류 코드가 키 중복 오류( `23505` )인 경우
     * `MyDuplicateKeyException` 을 새로 만들어서 서비스 계층에 던진다.
     * 나머지 경우 기존에 만들었던 `MyDbException` 을 던진다
     */

    Service service;
    Repository repository;

    @BeforeEach
    void beforeEach() {
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void test() {
        service.create("aaa");
        service.create("aaa");
    }

    @AfterEach
    void afterEach(){

    }

    @RequiredArgsConstructor
    static class Service {

        private final Repository repository;


        public void create(String memberId) {
            Member member = new Member(memberId, 0);
            try {
                repository.save(member);
                log.info("saveId = {}", memberId);
            } catch (MyDuplicateKeyException e) {
                log.info("중복예외, 키 복구시도");
                String retryId = generateNewId(memberId);
                log.info("generateNewId = {}", retryId);

                create(retryId);
            } catch (MyDBException e) {
                log.info("DB 예외 발생 ", e);
            }
        }


        private String generateNewId(String memberId) {
            return memberId + new Random().nextInt(10000);
        }

    }

    @RequiredArgsConstructor
    static class Repository {

        private final DataSource dataSource;

        public void save(Member member) {
            String sql = "insert into member(member_id, money) values (?, ?)";
            Connection conn = null;
            PreparedStatement pstmt = null;

            try {
                conn = dataSource.getConnection();
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == 23505) {
                    throw new MyDuplicateKeyException(e);
                }
                throw new MyDBException(e);
            } finally {
                JdbcUtils.closeConnection(conn);
                JdbcUtils.closeStatement(pstmt);
            }

        }


    }
}
