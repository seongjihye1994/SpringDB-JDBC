package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * JDBC - DriverManager 사용해서 개발
 */
@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {

        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null; // db 커넥션 맺는 애

        PreparedStatement pstmt = null; // db에 쿼리 날려주는 애

        try {
            con = getConnection(); // 커녁션 획득
            pstmt = con.prepareStatement(sql); // 쿼리 날림

            pstmt.setString(1, member.getMemberId()); // 쿼리 조건절 파라미터 바인딩
            pstmt.setInt(2, member.getMoney()); // 쿼리 조건절 파라미터 바인딩

            pstmt.executeUpdate(); // 쿼리 실행

            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }

        // finally 구문에서 마지막에 사용한 순서대로 객체를 닫아준다.
        // 닫아주지 않으면 커넥션이 닫기지 않고 메모리에서 떠다니게 된다.
        // pstmt.close(), con.close() 순서

        // 근대 문제는, 만약 pstmt.close() 를 호출했는데 exception 이 뜨면,
        // con.close()는 호출되지 않는다.
        // 이 문제를 해결하기 위해 코드를 아래처럼 작성해야 한다.

    }

    // 사용한 객체 닫아주기기
    private void close(Connection con, Statement stmt, ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close(); // 만약 여기서 SQL Exception이 터져도
            } catch (SQLException e) { // catch 에서 잡아주기 때문에 아래까지 영향이 미치지 않는다.
                log.info("error", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
