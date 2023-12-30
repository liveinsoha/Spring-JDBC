package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.junit.jupiter.api.Assertions.*;

class MemberRepositoryV1Test {

    MemberRepositoryV1 memberRepositoryV1;

    @BeforeEach
    void beforeEach() {

        /**
         * DataSource` 의존관계 주입
         * 외부에서 `DataSource` 를 주입 받아서 사용한다. 이제 직접 만든 `DBConnectionUtil` 을 사용하지 않아도 된다.
         * `DataSource` 는 표준 인터페이스 이기 때문에 `DriverManagerDataSource` 에서
         * `HikariDataSource` 로 변경되어도 해당 코드를 변경하지 않아도 된다.
         */
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MyPool");
        /**
         * 커넥션 풀 사용시 `conn0` 커넥션이 재사용 된 것을 확인할 수 있다.
         * 테스트는 순서대로 실행되기 때문에 커넥션을 사용하고 다시 돌려주는 것을 반복한다. 따라서 `conn0` 만 사용된다.
         * 웹 애플리케이션에 동시에 여러 요청이 들어오면 여러 쓰레드에서 커넥션 풀의 커넥션을 다양하게 가져가는 상황
         * 을 확인할 수 있다.
         */

        memberRepositoryV1 = new MemberRepositoryV1(dataSource);
    }

    @Test
    void test() throws SQLException {
        Member member = new Member("aaa", 123);
        memberRepositoryV1.save(member);

        Member aaa = memberRepositoryV1.findById("aaa");

        memberRepositoryV1.update("aaa", 321);

        memberRepositoryV1.delete("aaa");
    }

}