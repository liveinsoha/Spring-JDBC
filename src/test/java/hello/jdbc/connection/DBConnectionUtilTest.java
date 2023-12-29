package hello.jdbc.connection;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DBConnectionUtilTest {

    @Test
    void test() {
        Connection connection = DBConnectionUtil.getConnection();
        assertThat(connection).isNotNull();
    }

}