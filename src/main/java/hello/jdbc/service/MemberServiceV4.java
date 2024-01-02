package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;


@Slf4j
@RequiredArgsConstructor
public class MemberServiceV4 {


    /**
     * 체크 예외를 런타임 예외로 변환하면서 인터페이스와 서비스 계층의 순수성을 유지할 수 있게 되었다.
     * 덕분에 향후 JDBC에서 다른 구현 기술로 변경하더라도 서비스 계층의 코드를 변경하지 않고 유지할 수 있다.
     */
    private final MemberRepository memberRepository;

    /**
     * 순수한 비즈니스 로직만 남기고, 트랜잭션 관련 코드는 모두 제거했다.
     * 스프링이 제공하는 트랜잭션 AOP를 적용하기 위해 `@Transactional` 애노테이션을 추가했다.
     * `@Transactional` 애노테이션은 메서드에 붙여도 되고, 클래스에 붙여도 된다. 클래스에 붙이면 외부에서 호출
     * 가능한 `public` 메서드가 AOP 적용 대상이 된다
     */

    @Transactional
    public void transferMoney(String fromId, String toId, int money) {
        bizLogic(fromId, toId, money);
    }

    public void deleteAll(){
        memberRepository.deleteAll();
    }

    private void bizLogic(String fromId, String toId, int money)  {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validate(toId);
        memberRepository.update(toId, toMember.getMoney() + money);
    }


    public void join(Member member){
        memberRepository.save(member);
    }

    public Member findById(String memberId) {
        return memberRepository.findById(memberId);
    }

    private void validate(String toId) {
        if (toId.equals("ex")) {
            throw new IllegalStateException("예외 발생");
        }
    }
}
