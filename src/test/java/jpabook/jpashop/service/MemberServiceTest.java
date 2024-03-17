package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) //스프링이랑 엮어서 실행
@SpringBootTest //스프링 부트를 띄운 상태( 컨테이너 안에서 테스트돌림) 없으면 @Autowired 사용 불가
@Transactional //트랜젝션
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em; //이렇게 엔티티 매너지 가져오면 이걸로 플러시 가능

    @Test
    public void 회원가입() throws Exception{

        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long savedId = memberService.join(member);

        //then
        em.flush(); //이러면 쿼리 나가는거 보임 하지만 롤백은가능
        assertEquals(member,memberRepository.findOne(savedId));
    }
    @Test(expected = IllegalStateException.class) //이걸로 예외가 뜨면 처리 가능 예외 떠서
                                                    //IllegalStateException 뜨면 테스트 성공
    public void 중복_회원_예외() throws Exception {

        //given
        Member member1 = new Member();
        member1.setName("choi");

        Member member2 = new Member();
        member2.setName("choi");


        //when
        memberService.join(member1);
        memberService.join(member2); //예외가 발생해야함!!


        //then
        fail("예외가 발생해야함");
    }    

}