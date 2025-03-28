package com.hashedin.huSpark.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hashedin.huSpark.dto.GroupMemberCountDTO;
import com.hashedin.huSpark.dto.GroupPostCountDTO;
import com.hashedin.huSpark.entity.Group;

import jakarta.transaction.Transactional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByCreatorId(Long creatorId);

    //Circular query fixed here: don't join with Posts here since that leads to circular dependency problems with Posts
    // User →(is part of) Group →(has) Group Members → User →(creates) Post →(references) Group
    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.members WHERE g.id = :groupId")
    Optional<Group> findByIdWithMembers(@Param("groupId") Long groupId);

    //Circular query fixed here: don't join with Members here since that leads to circular dependency problems with Members
    // User →(is part of) Group →(has) Group Members → User →(creates) Post →(references) Group
    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.posts WHERE g.id = :groupId")
    Optional<Group> findByIdWithPosts(@Param("groupId") Long groupId);

    //Fixed query performance - avoid subqueries or unnecesaary joins
    // Fetch Groups where the user is a member (Optimized Query)
    @Query("SELECT DISTINCT g FROM Group g JOIN g.members m WHERE m.id = :userId")
    List<Group> findGroupsByMemberId(@Param("userId") Long userId);

    //Fixed query performance - avoid LEFT JOIN on same table since it leads to unnecessary duplicates
    //Fetch Public or Private Groups where User is a Member (Performance Optimized)
    @Query("SELECT g FROM Group g WHERE g.isPrivate = false OR " +
                "g.id IN (SELECT gm.id FROM Group gm JOIN gm.members m WHERE m.id = :userId)")
    /*
           """
           SELECT g FROM Group g 
            WHERE g.isPrivate = false 
            OR EXISTS (SELECT 1 FROM g.members m WHERE m.id = :userId)
           """
    */
    Page<Group> findVisibleGroups(@Param("userId") Long userId, Pageable pageable);

    //Fixed query sorting - add order by for search type queries
    //Search Groups by Name (Optimized Sorting)
    @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY g.name ASC")
    Page<Group> searchGroups(@Param("searchTerm") String searchTerm, Pageable pageable);

    //Fixed query - Fetch Joins are not allowed in DTO queries
    // Count Members Per Group (DTO Query - Fetch Joins Removed)
    @Query("SELECT new com.hashedin.huSpark.dto.GroupMemberCountDTO(g, COUNT(m)) " +
        "FROM Group g LEFT JOIN g.members m " +
        "GROUP BY g ORDER BY COUNT(m) DESC")
    Page<GroupMemberCountDTO> findGroupsOrderedByMemberCount(Pageable pageable);

    // Count Posts Per Group (DTO Query - Optimized)
    @Query("SELECT new com.hashedin.huSpark.dto.GroupPostCountDTO(g, COUNT(p)) FROM Group g LEFT JOIN g.posts p GROUP BY g ORDER BY COUNT(p) DESC")
    Page<GroupPostCountDTO> findGroupsOrderedByPostCount(Pageable pageable);

    // Fetch Group by ID (Without Relations)
    @Query("SELECT g FROM Group g WHERE g.id = :groupId")
    Optional<Group> findByIdWithoutRelations(@Param("groupId") Long groupId);

    // Fetch Group with Members
    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.members WHERE g.id = :groupId")
    Optional<Group> findGroupWithMembers(@Param("groupId") Long groupId);

    // Fetch Group with Posts
    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.posts WHERE g.id = :groupId")
    Optional<Group> findGroupWithPosts(@Param("groupId") Long groupId);

    boolean existsByName(String name);

    @EntityGraph(attributePaths = {"creator"}) // Ensure `creator` is fetched eagerly
    @Query("SELECT g FROM Group g WHERE g.id = :id")
    Optional<Group> findByIdWithCreator(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Post p WHERE p.group.id = :groupId")
    void deletePostsByGroupId(@Param("groupId") Long groupId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM group_members WHERE group_id = :groupId", nativeQuery = true)
    void deleteGroupMembers(@Param("groupId") Long groupId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Report r WHERE r.post.id IN (SELECT p.id FROM Post p WHERE p.group.id = :groupId)")
    void deleteReportsByGroupId(@Param("groupId") Long groupId);
}