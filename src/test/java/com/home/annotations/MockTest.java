package com.home.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 스프링 컨텍스트를 띄우지 않는 순수 JUnit5 + Mockito 기반 단위 테스트
 * DB, Spring Bean 없이 순수 자바 객체만 테스트할 때 사용
 * */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith({MockitoExtension.class})
public @interface MockTest {
}
