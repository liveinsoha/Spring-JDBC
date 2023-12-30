package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;

import java.sql.SQLException;

public class MemberService {

    private final MemberRepositoryV1 memberRepositoryV1;

    public MemberService(MemberRepositoryV1 memberRepositoryV1) {
        this.memberRepositoryV1 = memberRepositoryV1;
    }

    public void transferMoney(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepositoryV1.findById(fromId);
        Member toMember = memberRepositoryV1.findById(toId);

        memberRepositoryV1.update(fromId, fromMember.getMoney() - money);
        validate(toId);
        memberRepositoryV1.update(toId, fromMember.getMoney() + money);
    }

    public void join(Member member) throws SQLException {
        memberRepositoryV1.save(member);
    }

    public Member findById(String memberId){
        return memberRepositoryV1.findById(memberId);
    }

    private void validate(String toId) {
        if (toId.equals("ex")) {
            throw new IllegalStateException("예외 발생");
        }
    }
}
