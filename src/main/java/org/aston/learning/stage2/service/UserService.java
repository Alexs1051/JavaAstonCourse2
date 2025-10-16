package org.aston.learning.stage2.service;

import org.aston.learning.stage2.entity.User;
import org.aston.learning.stage2.exception.UserServiceException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(String name, String email, Integer age) throws UserServiceException;
    Optional<User> getUserById(Long id) throws UserServiceException;
    List<User> getAllUsers() throws UserServiceException;
    User updateUser(Long id, String name, String email, Integer age) throws UserServiceException;
    void deleteUser(Long id) throws UserServiceException;
    boolean userExists(Long id) throws UserServiceException;
    boolean isValidEmail(String email);
    boolean isValidAge(Integer age);
}