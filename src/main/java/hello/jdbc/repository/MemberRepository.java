package hello.jdbc.repository;

import hello.jdbc.domain.Member;

import java.util.List;

public interface MemberRepository {

    void save(Member member);

    Member findById(String memberId);

    void delete(String memberId);

    void update(String memberId, int money);

    void deleteAll();
}
