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

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

    @Override
    public User save(User user) throws UserServiceException {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();

            logger.info("User saved successfully: {}", user.getEmail());
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            logger.error("Error saving user: {}", user.getEmail(), e);
            throw new UserServiceException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws UserServiceException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by id: {}", id, e);
            throw new UserServiceException("Failed to find user by id", e);
        }
    }

    @Override
    public List<User> findAll() throws UserServiceException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new UserServiceException("Failed to find all users", e);
        }
    }

    @Override
    public User update(User user) throws UserServiceException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User updatedUser = session.merge(user);
            transaction.commit();
            logger.info("User updated successfully: {}", user.getEmail());
            return updatedUser;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating user: {}", user.getEmail(), e);
            throw new UserServiceException("Failed to update user", e);
        }
    }

    @Override
    public void delete(Long id) throws UserServiceException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                logger.info("User deleted successfully: {}", id);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting user: {}", id, e);
            throw new UserServiceException("Failed to delete user", e);
        }
    }
}