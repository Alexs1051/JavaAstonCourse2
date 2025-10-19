package org.aston.learning.stage2.util;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

class HibernateUtilTest {

    @Test
    @DisplayName("Should create SessionFactory successfully")
    void getSessionFactory_ShouldReturnValidInstance() {
        // When
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        // Then
        assertThat(sessionFactory).isNotNull();
        assertThat(sessionFactory.isOpen()).isTrue();

        assertThatNoException().isThrownBy(() -> {
            sessionFactory.openSession().close();
        });
    }

    @Test
    @DisplayName("Should return same SessionFactory instance")
    void getSessionFactory_ShouldReturnSingleton() {
        // When
        SessionFactory firstInstance = HibernateUtil.getSessionFactory();
        SessionFactory secondInstance = HibernateUtil.getSessionFactory();

        // Then
        assertThat(firstInstance).isSameAs(secondInstance);
    }

    @Test
    @DisplayName("Should shutdown without errors")
    void shutdown_ShouldCloseSessionFactory() {
        // Given
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        // When
        HibernateUtil.shutdown();

        // Then
        assertThat(sessionFactory.isOpen()).isFalse();
    }

    @Test
    @DisplayName("Should handle shutdown of already closed factory")
    void shutdown_AlreadyClosed_ShouldNotThrow() {
        // Given
        HibernateUtil.shutdown(); // First close

        // When & Then
        assertThatNoException().isThrownBy(HibernateUtil::shutdown); // Second close
    }
}