package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired MemberJpaRepository memberJpaRepository;
    @PersistenceContext
    EntityManager em;
    @Autowired TeamRepository teamRepository;

    @Test
    void testMember() {
        //given
        Member member = new Member("memberA");
        //when
        Member savedMember = memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.find(member.getId());
        //then
        assertThat(savedMember.getId()).isEqualTo(findMember.getId());
        assertThat(savedMember.getId()).isEqualTo(findMember.getId());
        assertThat(savedMember).isSameAs(findMember);
    }

    @Test
    void findMemberByUsernameAndAge() {
        //given
        Team teamA = getTeam("TeamA");
        Team teamB = getTeam("TeamB");
        saveMember("member1", 15, teamA);
        saveMember("member", 25, teamA);
        saveMember("member", 35, teamB);
        saveMember("member", 45, teamB);
        //when
        List<Member> olderMembers = memberJpaRepository.findByUsernameAndAgeGreaterThen("member", 30);
        //then
        assertThat(olderMembers.get(0).getAge()).isEqualTo(35);
        assertThat(olderMembers).hasSize(2);
    }

    @Test
    void bulkUpdate() {
        //given
        teamAndMemberSet();

        //when
        int queryResult = memberJpaRepository.bulkAgePlus();
        em.flush();
        em.clear();
        List<Member> members = memberJpaRepository.findAll();
        //then
        assertThat(queryResult).isEqualTo(10);


        System.out.println("members.get(0).getId() = " + members.get(0).getId());
        System.out.println("members = " + members.get(0).getAge());
        assertThat(members.get(0).getAge()).isEqualTo(16);
    }

    private void teamAndMemberSet() {
        Team teamA = getTeam("TeamA");
        for (int i = 1; i < 11; i++) {
            saveMember("member" + i, 15, teamA);
        }
    }


    private Member saveMember(String username, int age, Team team) {
        Member member = Member.createMember(username, age, team);
        return memberJpaRepository.save(member);
    }

    private Team getTeam(String teamName) {
        Team team = new Team(teamName);
        return teamRepository.save(team);
    }
}