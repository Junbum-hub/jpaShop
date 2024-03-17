package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional //이게 있어야 트렌젝션안에서 JPA 사용
//@AllArgsConstructor 얘도 생성자를 만들어 주긴한데 모든 필드에 대해 생성자를 만들어줌
@RequiredArgsConstructor //얘가 생성자를 알아서 만들어준다 final 있는 필드의 생성자를 만들어줌
                         // 생성자주입 자동으로 해줌
public class MemberService {


    private final MemberRepository memberRepository; //final 을 넣어야 나중에 오류로 확인 가능

//    @Autowired
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    //회원 가입
    @Transactional
    public Long join(Member member) {

        validateDuplicateMember(member); //중복 회원 검증
        memberRepository.save(member);
        return member.getId();

    }

    private void validateDuplicateMember(Member member) {
        //EXCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다");
        } //사실 회원의 이름은 유니크 제약조건으로 써야한다 그래야 같은 회원으로 가입이 불가하다

    }
    //회원 전체 조회
    @Transactional(readOnly = true) //읽기, 조회기능에는 readOnly 를 넣어주는게 좋다
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {

        Member member = memberRepository.findOne(id);
        member.setName(name);

    }
}
