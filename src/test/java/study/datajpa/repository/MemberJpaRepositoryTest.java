package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired MemberJpaRepository memberJpaRepository;

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
        Member member1 = createMember("member1", 15, teamA);
        Member member2 = createMember("member", 25, teamA);
        Member member3 = createMember("member", 35, teamB);
        Member member4 = createMember("member", 45, teamB);

        //when
        List<Member> olderMembers = memberJpaRepository.findByUsernameAndAgeGreaterThen("member", 30);

        //then
        assertThat(olderMembers.get(0).getAge()).isEqualTo(35);
        assertThat(olderMembers).hasSize(2);

    }

    private Member createMember(String username, int age, Team team) {
        Member member = Member.createMember(username, age, team);
        return memberJpaRepository.save(member);
    }

    private Team getTeam(String teamName) {
        Team team = new Team(teamName);
        return teamRepository.save(team);
    }

}