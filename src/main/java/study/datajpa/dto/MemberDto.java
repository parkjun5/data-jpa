package study.datajpa.dto;

import lombok.Getter;
import lombok.Setter;
import study.datajpa.entity.Member;

@Getter
@Setter
public class MemberDto {

    private Long memberId;
    private String name;
    private String teamName;

    public MemberDto(Long memberId, String name, String teamName) {
        this.memberId = memberId;
        this.name = name;
        this.teamName = teamName;
    }

    public MemberDto(Member member) {
        this.memberId = member.getId();
        this.name = member.getUsername();
    }
}
