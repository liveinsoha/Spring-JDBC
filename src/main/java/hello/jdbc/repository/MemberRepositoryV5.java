package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

public class MemberRepositoryV5 implements MemberRepository {

    /**
     * JdbcTemplate` 은 JDBC로 개발할 때 발생하는 반복을 대부분 해결해준다. 그 뿐만 아니라 지금까지 학습했던,
     * **트랜잭션을 위한 커넥션 동기화**는 물론이고, 예외 발생시 **스프링 예외 변환기**도 자동으로 실행해준다.
     */

    JdbcTemplate jdbcTemplate;

    public MemberRepositoryV5(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void save(Member member) {
        String sql = "insert into member(member_id, money) values (?, ?)";
        jdbcTemplate.update(sql, member.getMemberId(), member.getMoney());
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper(), memberId);
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id = ?";
        jdbcTemplate.update(sql, memberId);
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money = ? where member_id = ?";
        jdbcTemplate.update(sql, money, memberId);
    }

    @Override
    public void deleteAll() {
        String sql = "delete from member";
        jdbcTemplate.update(sql);
    }

    private RowMapper<Member> rowMapper() {
        return ((rs, rowNum) -> {
            String member_id = rs.getString("member_id");
            int money = rs.getInt("money");
            return new Member(member_id, money);
        });
    }
}
