package com.user_service.User.Service.repository;

import com.user_service.User.Service.entity.User;
import com.user_service.User.Service.service.UserService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long>{
    /** Used by AuthServiceImpl.login() to find a user by email. */
    Optional<User> findByEmail(String email);

    /** Used by AuthServiceImpl.register() to check for duplicate emails. */
    boolean existsByEmail(String email);

}
