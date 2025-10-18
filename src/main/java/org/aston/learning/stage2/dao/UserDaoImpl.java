package org.aston.learning.stage2.dao;

import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;
import org.aston.learning.stage2.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
    private final SessionFactory sessionFactory;

    // Constructs
    public UserDaoImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

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
        try (Session session = sessionFactory.openSession()) {
            User user = session.find(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by id: {}", id, e);
            throw new UserServiceException("Failed to find user by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() throws UserServiceException {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new UserServiceException("Failed to find all users: " + e.getMessage(), e);
        }
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
            } else {
                throw new UserServiceException("User not found with ID: " + id);
            }
        });
    }

    private <T> T executeInTransaction(Function<Session, T> function) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            T result = function.apply(session);

            transaction.commit();
            return result;

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new UserServiceException("Database operation failed: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private void executeInTransactionVoid(Consumer<Session> consumer) {
        executeInTransaction(session -> {
            consumer.accept(session);
            return null;
        });
    }
}