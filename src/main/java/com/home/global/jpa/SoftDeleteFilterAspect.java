package com.home.global.jpa;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class SoftDeleteFilterAspect {

	private final EntityManager entityManager;

	@Around("@within(transactional) || @annotation(transactional)")
	public Object applySoftDeleteFilter(ProceedingJoinPoint pjp,
		Transactional transactional) throws Throwable {

		Session session = entityManager.unwrap(Session.class);

		boolean alreadyEnabled =
			session.getEnabledFilter("softDeleteFilter") != null;

		if (!alreadyEnabled) {
			session.enableFilter("softDeleteFilter");
		}

		try {
			return pjp.proceed();
		} finally {
			if (!alreadyEnabled) {
				session.disableFilter("softDeleteFilter");
			}
		}
	}
}
