package study.datajpa.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.repository.MemberJpaRepository;
import study.datajpa.repository.TeamJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class EntityTest {

    @PersistenceContext EntityManager em;
    @Autowired MemberJpaRepository memberJpaRepository;
    @Autowired TeamJpaRepository teamJpaRepository;

    @Test
    @DisplayName("엔티티 BASIC TEST")
    void memberFindAllTest() {
        //given
        Team teamA = getTeam("TeamA");
        Team teamB = getTeam("TeamB");
        Member member1 = createMember("member1", 15, teamA);
        Member member2 = createMember("member2", 25, teamA);
        Member member3 = createMember("member3", 35, teamB);
        Member member4 = createMember("member4", 45, teamB);

        //when
        List<Member> members = memberJpaRepository.findAll();
        Long memberSize = memberJpaRepository.count();

        //then
        assertThat(memberSize).isEqualTo(4);

        em.remove(member1);
        em.remove(member2);

        Long removedSize = memberJpaRepository.count();
        assertThat(removedSize).isEqualTo(2);


        member3.setUsername("Dirty Checking");
        em.flush();

        Member updatedMember = memberJpaRepository.findOptionalMember(member3.getId()).orElse(null);
        assert updatedMember != null;
        assertThat(updatedMember.getUsername()).isEqualTo("Dirty Checking");
    }

    @Test
    @DisplayName("엔티티 기본 테스트")
    void memberTest() {
        //given
        Team teamA = getTeam("TeamA");
        Team teamB = getTeam("TeamB");

        Member member1 = createMember("memberA", 15, teamA);
        Member member3 = createMember("member3", 35, teamB);

        em.flush();
        //when
        Member findMember1 = em.find(Member.class, member1.getId());

        Member findMember3 = em.find(Member.class, member3.getId());

        //then
        assertThat(findMember1).isSameAs(member1);
        assertThat(findMember3.getTeam()).isSameAs(teamB);
    }

    private Member createMember(String username, int age, Team team) {
        Member member = Member.createMember(username, age, team);
        return memberJpaRepository.save(member);
    }

    private Team getTeam(String teamName) {
        Team team = new Team(teamName);
        return teamJpaRepository.save(team);
    }
}