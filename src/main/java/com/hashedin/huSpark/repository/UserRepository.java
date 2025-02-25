package com.hashedin.huSpark.repository;

import com.hashedin.huSpark.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.dateOfBirth IS NOT NULL AND DAY(u.dateOfBirth) = DAY(CURRENT_DATE) AND MONTH(u.dateOfBirth) = MONTH(CURRENT_DATE)")
    List<User> findUsersWithBirthdayToday();

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT DATE(u.createdAt) as date, COUNT(u) as count FROM User u GROUP BY DATE(u.createdAt) ORDER BY date")
    List<Object[]> countUsersByCreationDate();
}
