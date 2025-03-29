package com.hashedin.huSpark.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hashedin.huSpark.dto.UserCreationDateDTO;
import com.hashedin.huSpark.entity.User;
import jakarta.transaction.Transactional;

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

    @Query("SELECT new com.hashedin.huSpark.dto.UserCreationDateDTO(DATE(u.createdAt), COUNT(u)) FROM User u GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
    List<UserCreationDateDTO> countUsersByCreationDate();

    @Modifying
    @Transactional
    @Query(value="DELETE FROM group_members WHERE user_id = :userId", nativeQuery=true)
    void deleteGroupMembersByUserId(@Param("userId") Long userId);
}
