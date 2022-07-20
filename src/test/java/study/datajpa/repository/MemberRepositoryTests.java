package study.datajpa.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberRepositoryTests {
    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("JPA 리파지토리 테스트")
    void testJpaDataRepository() throws Exception {
        //given
        Member member = new Member("memberB");
        //when
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).get();
        //then
        assertThat(savedMember).isSameAs(findMember);

    }

}
