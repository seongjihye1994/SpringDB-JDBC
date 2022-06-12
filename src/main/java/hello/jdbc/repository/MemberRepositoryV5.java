package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

/**
 * JDBCTemplate 사용
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository {

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member) {

        String sql = "insert into member(member_id, money) values (?, ?)";

        int updated = template.update(sql, member.getMemberId(), member.getMoney());

        log.info("updated = {}", updated);

        return member;

    }

    @Override
    public Member findById(String memberId) {

        String sql = "select * from member where member_id = ?";

        // 한 건 조회는 queryForObject 사용
        return template.queryForObject(sql, memberRowMapper(), memberId);
    }

    @Override
    public void delete(String memberId) {

        String sql = "delete from member where member_id = ?";

        template.update(sql, memberId);

    }

    @Override
    public void update(String memberId, int money) {

        String sql = "update member set money = ? where member_id = ?";

        template.update(sql, money, memberId);

    }

    // sql 쿼리 결과가 rs 에 담기는데, 이 rs 에 담긴 값을 member에 세팅해서 member를 넘김
    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));

            return member;
        };
    }
}
