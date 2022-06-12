package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionSubclassTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    // dataSource 사용을 위해 의존성 주입
    private final DataSource dataSource;

    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource); // 어떤 디비를 사용하는지 찾아서 사용해야 하기 때문에 dataSource 를 파라미터로 넘긴다.
    }

    @Override
    public Member save(Member member) {

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
            throw exTranslator.translate("save", sql, e); // 스프링 제공 예외 변환기 사용
        } finally {
            close(con, pstmt, null);
        }
    }

    @Override
    public Member findById(String memberId) {

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
            throw exTranslator.translate("findById", sql, e); // 스프링 제공 예외 변환기 사용
        } finally {
            close(con, pstmt, rs);
        }
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection(); // 커녁션 획득
            pstmt = con.prepareStatement(sql); // 쿼리 날림

            pstmt.setString(1, memberId); // 쿼리 조건절 파라미터 바인딩

            pstmt.executeUpdate();// 쿼리 실행

        } catch (SQLException e) {
            throw exTranslator.translate("delete", sql, e); // 스프링 제공 예외 변환기 사용
        } finally {
            close(con, pstmt, null);
        }

    }

    @Override
    public void update(String memberId, int money) {
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
            throw exTranslator.translate("update", sql, e); // 스프링 제공 예외 변환기 사용
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
