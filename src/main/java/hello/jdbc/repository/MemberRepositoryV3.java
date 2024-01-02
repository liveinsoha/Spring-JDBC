package hello.jdbc.repository;


import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

@Slf4j
public class MemberRepositoryV3 {

    private final DataSource dataSource;
    /**
     * 커넥션을 파라미터로 전달하는 부분이 모두 제거
     */

    /**
     * DataSource를 외부로부터 주입받아서 사용한다.
     * Repository입장에서는 구현체를 모르고 DataSource인터페이스에 의존하기 때문에 구현체가 변경되어도 Repository의 코드를 바꿀 필요 없다
     * DriverManagerDataSource가 오든 커넥션 풀을 사용하는 HikariDataSource가 오든 주입만 바뀌고 Repository의 코드는 변경되지않는다.
     *
     * @param dataSource
     */

    public MemberRepositoryV3(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;
        /**
         * try밖에 null로 선언하는 이유는 finally에서 close할 떄 호출해야하기 떄문이다
         */

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("result = {}", resultSize);

        } catch (SQLException e) {
            log.error("error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void deleteAll() throws SQLException {
        String sql = "delete from member";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            int resultSize = pstmt.executeUpdate();
            log.info("deleteAll result = {}", resultSize);
        } catch (SQLException e) {
            log.error("error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }

    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money = ? where member_id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate(); //딱 찍어서 조회하기 때문에 한개만 영향 받을 수 있다
            log.info("result = {}", resultSize);
        } catch (SQLException e) {
            log.error("error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }

    }


    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";
        Connection connection = null;
        PreparedStatement pstmt = null;
        /**
         * PreparedStatement` 는 `Statement` 의 자식 타입인데, `?` 를 통한 파라미터 바인딩을 가능하게 해준다.
         * 참고로 SQL Injection 공격을 예방하려면 `PreparedStatement` 를 통한 파라미터 바인딩 방식을 사용해야
         * 한다.
         */

        try {
            connection = getConnection();
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            log.info("save connection = {}", connection);
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(connection, pstmt, null);
            /**
             * 중간에 예외가 발생해도 항상 close가 호출될 수 있도록 finally에 넣어준다.
             * 예전에는 다 순서 지켜서 했지만 최근에는 대신 수행해주는 기술들이 많다
             */
        }
    }

    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, memberId);
            rs = preparedStatement.executeQuery();
            log.info("findById connection = {}", con);
            if (rs.next()) {
                String member_id = rs.getString("member_id");
                int money = rs.getInt("money");
                return new Member(member_id, money);
            } else {//rs에 데이터가 없는경우 false를 반환한다.
                throw new NoSuchElementException("해당 멤버 없음 memberId = " + memberId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(con, pstmt, rs);
        }
    }


    private void close(Connection connection, Statement stmt, ResultSet resultSet) {
        /**
         * `close()` 에서 `DataSourceUtils.releaseConnection()` 를 사용하도록 변경된 부분을 특히 주의해야
         * 한다. 커넥션을 `con.close()` 를 사용해서 직접 닫아버리면 커넥션이 유지되지 않는 문제가 발생한다. 이 커넥
         * 션은 이후 로직은 물론이고, 트랜잭션을 종료(커밋, 롤백)할 때 까지 살아있어야 한다.
         * `DataSourceUtils.releaseConnection()` 을 사용하면 커넥션을 바로 닫는 것이 아니다.
         * **트랜잭션을 사용하기 위해 동기화된 커넥션은 커넥션을 닫지 않고 그대로 유지해준다.**
         * 트랜잭션 동기화 매니저가 관리하는 커넥션이 없는 경우 해당 커넥션을 닫는다.
         */
        DataSourceUtils.releaseConnection(connection, dataSource);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeResultSet(resultSet);
    }

    private Connection getConnection() throws SQLException {
        /**
         * `DataSourceUtils.getConnection()` 는 다음과 같이 동작한다.
         * **트랜잭션 동기화 매니저가 관리하는 커넥션이 있으면 해당 커넥션을 반환한다.**
         * 트랜잭션 동기화 매니저가 관리하는 커넥션이 없는 경우 새로운 커넥션을 생성해서 반환한다.
         */
        Connection connection = DataSourceUtils.getConnection(dataSource);
        log.info("connection = {},connection.getClass() = {}", connection, connection.getClass());
        return connection;
    }
}
