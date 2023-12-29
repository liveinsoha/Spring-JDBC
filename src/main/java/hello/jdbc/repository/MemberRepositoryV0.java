package hello.jdbc.repository;


import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;


import java.sql.*;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class MemberRepositoryV0 {

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
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            log.info("error", e);
            throw new RuntimeException(e);
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            log.info("error", e);
            throw new RuntimeException(e);
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            log.info("error", e);
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DBConnectionUtil.getConnection();
    }
}
