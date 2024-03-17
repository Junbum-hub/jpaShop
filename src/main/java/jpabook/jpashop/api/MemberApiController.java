package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map((m -> new MemberDto(m.getName())))
                .collect(Collectors.toList());


        return new Result(collect.size(),collect);

    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        //이렇게 엔티티랑 api 랑 1대1로 매핑해서 사용하게되면 나중에 엔티티가 변경이되면 api 스펙 자체도
        //변경이 되므로 큰 장애가 발생할 수 있다. 왜냐하면 엔티티는 다른곳에서도 자주 사용하기 때문이다.
        //따라서 DTO 가 필요하다 애초에 엔티티를 밖에 노출시키는 것이 좋은것이 아니다 엔티티 자체를 파라미터에
        //노출시키지 말자
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        //이렇게 별도의 DTO를 사용하면 api 스펙이 바뀌지 않는다는 장점이 있다.
        Member member= new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id,request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(),findMember.getName());
    }

    @Data
    static class CreateMemberResponse {
        public CreateMemberResponse(Long id) {
            this.id = id;
        }

        private Long id;
    }

    @Data
    static class CreateMemberRequest{
        private String name;
    }

    @Data
    static class UpdateMemberRequest{
        private String name;

    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        String name;

    }
}
