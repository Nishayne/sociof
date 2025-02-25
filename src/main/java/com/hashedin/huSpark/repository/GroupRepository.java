package com.hashedin.huSpark.repository;

import com.hashedin.huSpark.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Group entity
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByCreatorId(Long creatorId);

    @Query("SELECT g FROM Group g WHERE g.id IN (SELECT gm.id FROM Group gm JOIN gm.members m WHERE m.id = :userId)")
    List<Group> findGroupsByMemberId(@Param("userId") Long userId);

    @Query("SELECT g FROM Group g WHERE (g.isPrivate = false OR g.id IN " +
            "(SELECT gm.id FROM Group gm JOIN gm.members m WHERE m.id = :userId))")
    Page<Group> findVisibleGroups(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Group> searchGroups(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT g, COUNT(m) as memberCount FROM Group g LEFT JOIN g.members m GROUP BY g ORDER BY memberCount DESC")
    Page<Object[]> findGroupsOrderedByMemberCount(Pageable pageable);

    @Query("SELECT g, COUNT(p) as postCount FROM Group g LEFT JOIN g.posts p GROUP BY g ORDER BY postCount DESC")
    Page<Object[]> findGroupsOrderedByPostCount(Pageable pageable);
}
