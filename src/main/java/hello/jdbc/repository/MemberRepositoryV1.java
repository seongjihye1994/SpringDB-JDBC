package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DataSource, JDBC Utils 사용해서 개발
 */
@Slf4j
public class MemberRepositoryV1 {

    // dataSource 사용을 위해 의존성 주입
    private final DataSource dataSource;

    public MemberRepositoryV1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;

        PreparedStatement pstmt = null;

        ResultSet rs = null; // 쿼리 결과 담고있는 통

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();

            if (rs.next()) { // 첫번째 데이터가 있냐? 있으면 t, 없으면 f
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));

                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money = ? where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection(); // 커녁션 획득
            pstmt = con.prepareStatement(sql); // 쿼리 날림

            pstmt.setInt(1, money); // 쿼리 조건절 파라미터 바인딩
            pstmt.setString(2, memberId); // 쿼리 조건절 파라미터 바인딩

            int resultSize = pstmt.executeUpdate();// 쿼리 실행
            log.info("resultSize={}", resultSize);

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection(); // 커녁션 획득
            pstmt = con.prepareStatement(sql); // 쿼리 날림

            pstmt.setString(1, memberId); // 쿼리 조건절 파라미터 바인딩

            pstmt.executeUpdate();// 쿼리 실행

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }

    }

    // JdbcUtils 를 사용한 객체 닫아주기
    private void close(Connection con, Statement stmt, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get Connection={}, class={}", con, con.getClass());
        return con;
    }
}
