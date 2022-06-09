package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 트랜잭션 - 트랜잭션 매니저 사용
 *
 * DataSourceUtils.getConnection()
 * DataSourceUtils.releaseConnection()
 *
 * 트랜잭션 매니저는 데이터소스를 통해 커넥션을 만들고 트랜잭션을 시작한다.
 * 커넥션 조회, 오토 커밋 false 등 모두 데이터 매니저가 직접 해준다.
 */
@Slf4j
public class MemberRepositoryV3 {

    // dataSource 사용을 위해 의존성 주입
    private final DataSource dataSource;

    public MemberRepositoryV3(DataSource dataSource) {
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

    // JdbcUtils 를 사용한 객체 닫아주기
    private void close(Connection con, Statement stmt, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);

        // *** 주의! ***
        // JdbcUtils.closeConnection(con);
        DataSourceUtils.releaseConnection(con, dataSource);
        // 트랜잭션 동기화에서 꺼낸 트랜잭션은 종료(close)하는 것이 아닌,
        // 트랜잭션 동기화 매니저에 다시 돌려줘야 한다.
    }

    private Connection getConnection() throws SQLException {

        // *** 주의! ***
        // 트랜잭션 동기화를 사용하려면 DataSourceUtils 를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        // 트랜잭션 동기화 매니저에서 커넥션을 꺼낸다.

        log.info("get Connection={}, class={}", con, con.getClass());
        return con;
    }
}
