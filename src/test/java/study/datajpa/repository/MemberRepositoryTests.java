package study.datajpa.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTests {
    @Autowired MemberRepository memberRepository;

    @Autowired TeamRepository teamRepository;

    @Test
    @DisplayName("JPA 리파지토리 테스트")
    void testJpaDataRepository() {
        //given
        System.out.println("memberRepository.getClass() = " + memberRepository.getClass());
        Member member = new Member("memberB");
        //when
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).orElse(null);
        //then
        assertThat(savedMember).isSameAs(findMember);
    }
    
    @Test
    void findMemberByUsernameAndAgeGreaterThan() {
        //given
        Team teamA = getTeam("TeamA");
        Team teamB = getTeam("TeamB");
        Member member1 = createMember("member1", 15, teamA);
        Member member2 = createMember("member", 25, teamA);
        Member member3 = createMember("member", 35, teamB);
        Member member4 = createMember("member", 45, teamB);

        //when
        List<Member> olderMembers = memberRepository.findMemberByUsernameAndAgeGreaterThanEqual("member", 30);

        //then
        assertThat(olderMembers.get(0).getAge()).isEqualTo(35);
        assertThat(olderMembers).hasSize(2);
    }

    private Member createMember(String username, int age, Team team) {
        Member member = Member.createMember(username, age, team);
        return memberRepository.save(member);
    }

    private Team getTeam(String teamName) {
        Team team = new Team(teamName);
        return teamRepository.save(team);
    }
}
