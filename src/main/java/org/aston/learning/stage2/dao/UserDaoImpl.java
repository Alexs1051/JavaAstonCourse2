package org.aston.learning.stage2.dao;

import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;
import org.aston.learning.stage2.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) throws UserServiceException {
        return executeInTransaction(session -> {
            session.persist(user);
            logger.info("User saved successfully: {}", user.getEmail());
            return user;
        });
    }

    @Override
    public Optional<User> findById(Long id) throws UserServiceException {
        return executeInTransaction(session -> {
            User user = session.find(User.class, id);
            return Optional.ofNullable(user);
        });
    }

    @Override
    public List<User> findAll() throws UserServiceException {
        return executeInTransaction(session -> {
            Query<User> query = session.createQuery("FROM User", User.class)
                    .setMaxResults(100)
                    .setFirstResult(0);
            return query.list();
        });
    }

    @Override
    public User update(User user) throws UserServiceException {
        return executeInTransaction(session -> {
            User updatedUser = session.merge(user);
            logger.info("User updated successfully: {}", user.getEmail());
            return updatedUser;
        });
    }

    @Override
    public void delete(Long id) throws UserServiceException {
        executeInTransactionVoid(session -> {
            User user = session.find(User.class, id);
            if (user != null) {
                session.remove(user);
                logger.info("User deleted successfully: {}", id);
            }
        });
    }

    private <T> T executeInTransaction(Function<Session, T> function) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            T result = function.apply(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Transaction failed", e);
            throw new UserServiceException("Database operation failed", e);
        }
    }

    private void executeInTransactionVoid(Consumer<Session> consumer) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            consumer.accept(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Transaction failed", e);
            throw new UserServiceException("Database operation failed", e);
        }
    }
}