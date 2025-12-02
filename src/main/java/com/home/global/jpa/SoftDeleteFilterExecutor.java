package com.home.global.jpa;

import java.util.function.Supplier;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class SoftDeleteFilterExecutor {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * softDeleteFilter를 끄고 콜백을 실행.
	 * 실행이 끝나면 이전 상태로 복원.
	 */
	public <T> T ignoreSoftDelete(Supplier<T> supplier) {
		Session session = entityManager.unwrap(Session.class);
		boolean wasEnabled = session.getEnabledFilter("softDeleteFilter") != null;

		if (wasEnabled) {
			session.disableFilter("softDeleteFilter");
		}

		try {
			return supplier.get();
		} finally {
			if (wasEnabled) {
				session.enableFilter("softDeleteFilter");
			}
		}
	}

	public void ignoreSoftDelete(Runnable runnable) {
		ignoreSoftDelete(() -> {
			runnable.run();
			return null;
		});
	}
}
