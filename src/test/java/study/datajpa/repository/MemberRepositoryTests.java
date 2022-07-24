package study.datajpa.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTests {
    @Autowired MemberRepository memberRepository;

    @Autowired TeamRepository teamRepository;

    @PersistenceContext EntityManager em;

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

    @Test
    void paging() throws Exception {
        //given
        기본맴버_세팅();

        int age = 15;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> pageResponse = memberRepository.findByAge(age, pageRequest);

        //then
        List<Member> content = pageResponse.getContent();

        assertThat(content.get(2).getUsername()).isEqualTo("member3");

        int pageNumber = pageResponse.getNumber();
        assertThat(pageNumber).isEqualTo(0);

        long totalElements = pageResponse.getTotalElements();
        assertThat(totalElements).isEqualTo(5);

        int totalPages = pageResponse.getTotalPages();
        assertThat(totalPages).isEqualTo(2);
        assertThat(pageResponse.isFirst()).isTrue();
        assertThat(pageResponse.hasNext()).isTrue();
        assertThat(pageResponse.isLast()).isFalse();
    }

    @Test
    void slice() throws Exception {
        //given
        기본맴버_세팅();

        int age = 15;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Slice<Member> pageResponse = memberRepository.findByAge(age, pageRequest);

        //then
        List<Member> content = pageResponse.getContent();

        assertThat(content.get(2).getUsername()).isEqualTo("member3");

        int pageNumber = pageResponse.getNumber();
        assertThat(pageNumber).isEqualTo(0);
        assertThat(pageResponse.isFirst()).isTrue();
        assertThat(pageResponse.hasNext()).isTrue();
        assertThat(pageResponse.isLast()).isFalse();
    }

    @Test
    void selectTop3() throws Exception {
        //given
        기본맴버_세팅();
        //when
        List<Member> top3ByAge = memberRepository.findTop3ByAge(15);

        //then
        assertThat(top3ByAge).hasSize(3);
        assertThat(top3ByAge.get(0).getUsername()).isEqualTo("member1");
    }

    @Test
    void pageWithDto() throws Exception {
        //given
        기본맴버_세팅();
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> memberPage = memberRepository.findByAge(15, pageRequest);
        Page<MemberDto> dtoPage = memberPage.map(m -> new MemberDto(m.getId(), m.getUsername(), m.getTeam().getName()));
        //then

        assertThat(dtoPage.getContent().get(0).getTeamName()).isEqualTo("TeamA");

        int pageNumber = dtoPage.getNumber();
        assertThat(pageNumber).isEqualTo(0);

        long totalElements = dtoPage.getTotalElements();
        assertThat(totalElements).isEqualTo(5);

        int totalPages = dtoPage.getTotalPages();
        assertThat(totalPages).isEqualTo(2);
        assertThat(dtoPage.isFirst()).isTrue();
        assertThat(dtoPage.hasNext()).isTrue();
        assertThat(dtoPage.isLast()).isFalse();
    }

    @Test
    @Rollback(value = false)
    void bulkUpdate() throws Exception {
        //given
        기본맴버_세팅();
        em.flush();
        em.clear();

        Thread.sleep(3000);
        System.out.println("============ 지연로딩만 읽나? ================");
        System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
        //when
        int queryResult = memberRepository.bulkAgePlus(15);
        List<Member> members = memberRepository.findAll();

        Thread.sleep(3000);
        System.out.println("============  더티채킹은 먹음 ================");
        System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
        members.get(2).setAge(3999);
        //then
        assertThat(queryResult).isEqualTo(10);
        assertThat(members.get(0).getAge()).isEqualTo(16);
    }

    @Test
    void findMemberLazy() throws Exception {
        //given
        Team teamC = getTeam("TeamC");
        Team teamD = getTeam("TeamD");

        createMember("Member1", 15, teamC);
        createMember("Member2", 15, teamD);
        기본맴버_세팅();
        //when
        em.flush();
        em.clear();
        List<Member> members = memberRepository.findAll();
        
        //then
        System.out.println("__________________________________");
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }

    @Test
    void findMemberFetch() throws Exception {
        //given
        Team teamC = getTeam("TeamC");
        Team teamD = getTeam("TeamD");

        createMember("Member1", 15, teamC);
        createMember("Member2", 15, teamD);
        기본맴버_세팅();
        //when
        em.flush();
        em.clear();
        List<Member> members = memberRepository.findMembersFetchJoin();

        //then
        System.out.println("__________________________________");
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }

    @Test
    void queryHint() throws Exception {
        //given
        Team teamA = getTeam("TeamA_");
        Member member = createMember("member", 15, teamA);
        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findReadOnlyByUsername(member.getUsername());
        findMember.setUsername("MEMBER!");
        em.flush();
        //then
    }

    @Test
    void lockTest() throws Exception {
        //given
        Team teamA = getTeam("TeamA_");
        Member member = createMember("member", 15, teamA);
        em.flush();
        em.clear();
        //when
        List<Member> members = memberRepository.findLockByUsername("member");
        members.get(0).setUsername("ASDASD");
        //then
        em.flush();

    }

    @Test
    void customRepository() throws Exception {
        //given
        Team teamA = getTeam("TeamA_");
        Member member = createMember("member", 15, teamA);
        em.flush();
        em.clear();
        //when

        List<Member> memberCustom = memberRepository.findMemberCustom();

        //then

        assertThat(memberCustom).hasSize(1);
    }

    private void 기본맴버_세팅() {
        for (int i = 1; i < 11; i++) {
            Team teamA = getTeam("TeamA_" + i);
            createMember("member" + i, 15, teamA);
        }
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
