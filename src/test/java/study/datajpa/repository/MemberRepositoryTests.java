package study.datajpa.repository;

import org.awaitility.Durations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
        createMember("member1",15, teamA);
        createMember("member", 25, teamA);
        createMember("member", 35, teamB);
        createMember("member", 45, teamB);

        //when
        List<Member> olderMembers = memberRepository.findMemberByUsernameAndAgeGreaterThanEqual("member", 30);

        //then
        assertThat(olderMembers.get(0).getAge()).isEqualTo(35);
        assertThat(olderMembers).hasSize(2);
    }
    @Test
    void queryTest() {
        //given
        Team teamA = getTeam("TeamA");
        Member member1 = createMember("member1", 15, teamA);
        //when
        List<Member> noUser = memberRepository.findUser(member1.getUsername(), member1.getAge() + 1);
        List<Member> findUser = memberRepository.findUser(member1.getUsername(), member1.getAge());
        //then
        assertThat(noUser).isEmpty();
        assertThat(findUser.get(0)).isEqualTo(member1);
    }

    @Test
    void findUserNames() {
        //given
        setMembers();
        //when
        List<String> userNames = memberRepository.findUserNames();
        //then
        assertThat(userNames).hasSize(10);
    }

    @Test
    void findMemberDto() {
        //given
        setMembers();
        //when
        List<MemberDto> memberDto = memberRepository.findMemberDto();
        //then
        assertThat(memberDto.get(0).getUsername()).isEqualTo("member1");
        assertThat(memberDto.get(0).getTeamName()).isEqualTo("TeamA");
    }

    @Test
    void findByNames() {
        //given
        setMembers();
        //when
        List<Member> byNames = memberRepository.findByNames(Arrays.asList("member3", "member7", "member10"));
        //then
        assertThat(byNames).hasSize(3);
        assertThat(byNames.get(0).getUsername()).isEqualTo("member3");
    }

    @Test
    void paging() {
        //given
        teamAndMemberSet();

        int age = 15;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> pageResponse = memberRepository.findByAge(age, pageRequest);

        //then
        List<Member> content = pageResponse.getContent();

        assertThat(content.get(2).getUsername()).isEqualTo("member7");

        int pageNumber = pageResponse.getNumber();
        assertThat(pageNumber).isZero();

        long totalElements = pageResponse.getTotalElements();
        assertThat(totalElements).isEqualTo(10);

        int totalPages = pageResponse.getTotalPages();
        assertThat(totalPages).isEqualTo(4);
        assertThat(pageResponse.isFirst()).isTrue();
        assertThat(pageResponse.hasNext()).isTrue();
        assertThat(pageResponse.isLast()).isFalse();
    }

    @Test
    void slice() {
        //given
        teamAndMemberSet();

        int age = 15;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Slice<Member> pageResponse = memberRepository.findByAge(age, pageRequest);

        //then
        List<Member> content = pageResponse.getContent();

        assertThat(content.get(2).getUsername()).isEqualTo("member7");

        int pageNumber = pageResponse.getNumber();
        assertThat(pageNumber).isZero();
        assertThat(pageResponse.isFirst()).isTrue();
        assertThat(pageResponse.hasNext()).isTrue();
        assertThat(pageResponse.isLast()).isFalse();
    }

    @Test
    void selectTop3() {
        //given
        teamAndMemberSet();
        //when
        List<Member> top3ByAge = memberRepository.findTop3ByAge(15);

        //then
        assertThat(top3ByAge).hasSize(3);
        assertThat(top3ByAge.get(0).getUsername()).isEqualTo("member1");
    }

    @Test
    void pageWithDto() {
        //given
        teamAndMemberSet();
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> memberPage = memberRepository.findByAge(15, pageRequest);
        Page<MemberDto> dtoPage = memberPage.map(m -> new MemberDto(m.getId(), m.getUsername(), m.getTeam().getName()));
        //then

        assertThat(dtoPage.getContent().get(0).getTeamName()).isEqualTo("TeamA_9");

        int pageNumber = dtoPage.getNumber();
        assertThat(pageNumber).isZero();

        long totalElements = dtoPage.getTotalElements();
        assertThat(totalElements).isEqualTo(10);

        int totalPages = dtoPage.getTotalPages();
        assertThat(totalPages).isEqualTo(4);
        assertThat(dtoPage.isFirst()).isTrue();
        assertThat(dtoPage.hasNext()).isTrue();
        assertThat(dtoPage.isLast()).isFalse();
    }

    @Test
//    @Rollback(value = false)
    void bulkUpdate() {
        //given
        teamAndMemberSet();
        em.flush();
        em.clear();

        await().pollDelay(Durations.TWO_SECONDS).until(() -> true);
        System.out.println("============ 지연로딩만 읽나? ================");
        System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
        //when
        int queryResult = memberRepository.bulkAgePlus(15);
        List<Member> members = memberRepository.findAll();
        await().pollDelay(Durations.TWO_SECONDS).until(() -> true);

        System.out.println("============  더티채킹은 먹음 ================");
        System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
        members.get(2).setAge(3999);
        //then
        assertThat(queryResult).isEqualTo(10);
        assertThat(members.get(0).getAge()).isEqualTo(16);
    }

    @Test
    void findMemberLazy() {
        //given
        Team teamC = getTeam("TeamC");
        Team teamD = getTeam("TeamD");

        createMember("Member1", 15, teamC);
        createMember("Member2", 15, teamD);
        teamAndMemberSet();
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

        assertThat(members).isNotEmpty();
    }

    @Test
    void findMemberFetch() {
        //given
        Team teamC = getTeam("TeamC");
        Team teamD = getTeam("TeamD");

        createMember("Member1", 15, teamC);
        createMember("Member2", 15, teamD);
        teamAndMemberSet();
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
        assertThat(members).isNotEmpty();

    }

    @Test
    void queryHint() {
        //given
        Team teamA = getTeam("TeamA_");
        Member member = createMember("member", 15, teamA);
        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findReadOnlyByUsername(member.getUsername());
        findMember.setUsername("MEMBER!");
        em.flush();
        em.clear();

        //then
        Member result = memberRepository.findById(findMember.getId()).orElse(Member.createMember("empty", 9999, null));
        assertThat(result.getUsername()).isEqualTo("member");

    }

    @Test
    void lockTest() {
        //given
        Team teamA = getTeam("TeamA_");
        createMember("member", 15, teamA);
        em.flush();
        em.clear();
        //when
        List<Member> members = memberRepository.findLockByUsername("member");
        members.get(0).setUsername("change");
        em.flush();
        em.clear();

        //then
        Member result = memberRepository.findById(members.get(0).getId()).orElse(Member.createMember("empty", 9999, null));

        assertThat(result.getUsername()).isEqualTo("change");
    }

    @Test
    void customRepository() {
        //given
        Team teamA = getTeam("TeamA_");
        createMember("member", 15, teamA);
        em.flush();
        em.clear();
        //when
        List<Member> memberCustom = memberRepository.findMemberCustom();

        //then
        assertThat(memberCustom).hasSize(1);
    }

    @Test
    void queryByExample() {
        //given
        Team teamA = getTeam("teamA");
        Member m1 = createMember("m1", 2, teamA);
        Member m2 = createMember("m2", 12, teamA);
        em.flush();
        em.clear();

        //when
        //Porbe
        Member member = new Member("m1");

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");

        Example<Member> example = Example.of(member, matcher);

        List<Member> members = memberRepository.findAll(example);
        //then
        assertThat(members).hasSize(1);
    }

    @Test
    void projections() {
        //given
        simpleMemberSet();

        //when
        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1");

        for (UsernameOnly usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly.getClass());
            System.out.println("usernameOnly = " + usernameOnly.getUsername());
        }
        //then
        assertThat(result.get(0).getUsername()).isEqualTo("m1 2");
    }

    @Test
    void projectionsDto() {
        //given
        simpleMemberSet();

        //when
        List<UsernameOnlyDto> result = memberRepository.findProjectionsDtoByUsername("m1");

        for (UsernameOnlyDto usernameOnlyDto : result) {
            System.out.println("usernameOnly = " + usernameOnlyDto.getClass());
            System.out.println("usernameOnly = " + usernameOnlyDto.getUsername());
        }
        //then
        assertThat(result.get(0).getUsername()).isEqualTo("m1");

    }

    @Test
    void projectionsWithGeneric() {
        //given
        simpleMemberSet();

        //when
        List<UsernameOnlyDto> usernameOnlyDtoList = memberRepository.findGenericByUsername("m1", UsernameOnlyDto.class);
        List<UsernameOnly> usernameOnlyList = memberRepository.findGenericByUsername("m1", UsernameOnly.class);
        List<NestedClosedProjections> nestedClosedProjections = memberRepository.findGenericByUsername("m1", NestedClosedProjections.class);

        //then
        assertThat(nestedClosedProjections.get(0).getUsername()).isEqualTo("m1");
        assertThat(nestedClosedProjections.get(0).getTeam().getName()).isEqualTo("teamA");
        assertThat(usernameOnlyDtoList.get(0).getUsername()).isEqualTo("m1");
        assertThat(usernameOnlyList.get(0).getUsername()).isEqualTo("m1 2");
    }

    private void setMembers() {
        Team teamA = getTeam("TeamA");
        for (int i = 1; i < 11; i++) {
            createMember("member" + i, 15 + i, teamA);
        }
    }

    private void simpleMemberSet() {
        Team teamA = getTeam("teamA");
        createMember("m1", 2, teamA);
        createMember("m2", 12, teamA);
        em.flush();
        em.clear();
    }

    private void teamAndMemberSet() {
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
