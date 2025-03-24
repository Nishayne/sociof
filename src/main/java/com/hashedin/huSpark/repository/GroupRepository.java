package com.hashedin.huSpark.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hashedin.huSpark.dto.GroupMemberCountDTO;
import com.hashedin.huSpark.dto.GroupPostCountDTO;
import com.hashedin.huSpark.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByCreatorId(Long creatorId);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.members LEFT JOIN FETCH g.posts WHERE g.id = :groupId")
    Optional<Group> findByIdWithMembers(@Param("groupId") Long groupId);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.members WHERE g.id IN (SELECT gm.id FROM Group gm JOIN gm.members m WHERE m.id = :userId)")
    List<Group> findGroupsByMemberId(@Param("userId") Long userId);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.members WHERE (g.isPrivate = false OR g.id IN " +
            "(SELECT gm.id FROM Group gm JOIN gm.members m WHERE m.id = :userId))")
    Page<Group> findVisibleGroups(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Group> searchGroups(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT new com.hashedin.huSpark.dto.GroupMemberCountDTO(g, COUNT(m)) FROM Group g LEFT JOIN FETCH g.members m GROUP BY g ORDER BY COUNT(m) DESC")
    Page<GroupMemberCountDTO> findGroupsOrderedByMemberCount(Pageable pageable);

    @Query("SELECT new com.hashedin.huSpark.dto.GroupPostCountDTO(g, COUNT(p)) FROM Group g LEFT JOIN g.posts p GROUP BY g ORDER BY COUNT(p) DESC")
    Page<GroupPostCountDTO> findGroupsOrderedByPostCount(Pageable pageable);

    boolean existsByName(String name);
}