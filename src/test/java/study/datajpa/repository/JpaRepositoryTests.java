package study.datajpa.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JpaRepositoryTests {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

    @Test
    @DisplayName("리파지토리 기본 테스트")
    void memberTest() {
        //given
        Team teamA = getTeam("TeamA");
        Team teamB = getTeam("TeamB");
        Member member1 = createMember("memberA", 15, teamA);
        Member member3 = createMember("member3", 35, teamB);

        //when
        Member findMember1 = memberRepository.findById(member1.getId()).orElseThrow(() -> new IllegalArgumentException("값이 없습니다."));
        Member findMember3 = memberRepository.findById(member3.getId()).orElse(null);

        //then
        assertThat(findMember1).isSameAs(member1);
        assert findMember3 != null;
        assertThat(findMember3.getTeam()).isSameAs(teamB);
    }

    @Test
    @DisplayName("리파지토리 CRUD TEST")
    void memberFindAllTest() {
        //given
        Team teamA = getTeam("TeamA");
        Team teamB = getTeam("TeamB");
        Member member1 = createMember("member1", 15, teamA);
        Member member2 = createMember("member2", 25, teamA);
        Member member3 = createMember("member3", 35, teamB);
        Member member4 = createMember("member4", 45, teamB);

        //when
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            assertThat(member.getId()).isNotNull();
        }
        Long memberSize = memberRepository.count();

        //then
        assertThat(memberSize).isEqualTo(4);

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        Long removedSize = memberRepository.count();
        assertThat(removedSize).isEqualTo(2);

        member3.setUsername("Dirty Checking");

        Member updatedMember = memberRepository.findById(member3.getId()).orElse(null);
        assert updatedMember != null;
        assertThat(updatedMember.getUsername()).isEqualTo("Dirty Checking");
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
