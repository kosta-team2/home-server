package com.home.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실제 DB를 사용하는 통합 테스트용 애노테이션
 * 서비스 & 리포지토리 & 트랜잭션이 모두 얽힌 도메인 시나리오를 실제 DB를 통해 검증할 때 사용
 * @see Transactional (기본적으로 각 테스트 메서드 종료 시 트랜잭션 롤백)
 * @see SpringBootTest
 * */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public @interface IntegrationTest {
}
