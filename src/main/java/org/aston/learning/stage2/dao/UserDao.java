package org.aston.learning.stage2.dao;

import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User save(User user) throws UserServiceException;
    Optional<User> findById(Long id) throws UserServiceException;
    List<User> findAll() throws UserServiceException;
    User update(User user) throws UserServiceException;
    void delete(Long id) throws UserServiceException;
}