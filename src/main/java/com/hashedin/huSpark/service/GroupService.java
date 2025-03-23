package com.hashedin.huSpark.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hashedin.huSpark.dto.GroupRequest;
import com.hashedin.huSpark.entity.Group;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.exception.UnauthorizedException;
import com.hashedin.huSpark.repository.GroupRepository;

/**
 * Service for group-related operations
 */
@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserService userService;

    /**
     * Create a new group
     * @param groupRequest Group creation request
     * @param creatorId ID of user creating the group
     * @return Created group
     */
    @Transactional
    @CacheEvict(value = {"groups_", "groupStats"}, allEntries = true)
    public Group createGroup(GroupRequest groupRequest, Long creatorId) {
        User creator = userService.findById(creatorId);

        Group group = Group.builder()
                .name(groupRequest.getName())
                .isPrivate(groupRequest.getIsPrivate())
                .creator(creator)
                .build();
        Group retVal = null;
        try {
            // Add creator as a member
            //Hibernate.initialize(group.getMembers()); // Explicitly initialize, prevent concurrency problems
            group.getMembers().add(creator);
            retVal = groupRepository.save(group);
            return retVal;
        }
        catch(Exception e)
        {
            System.err.println("createGroup exception  " + e.toString());
        }
        return retVal;
    }

    /**
     * Find a group by ID
     * @param id ID of group to find
     * @return Group
     * @throws ResourceNotFoundException if group is not found
     */
    public Group findById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
    }

    /**
     * Update a group
     * @param id ID of group to update
     * @param groupRequest Group update request
     * @param userId ID of user updating the group
     * @return Updated group
     */
    @Transactional
    @CacheEvict(value = {"groups_", "groupStats"}, allEntries = true)
    public Group updateGroup(Long id, GroupRequest groupRequest, Long userId) {
        Group group = findById(id);

        // Check if current user is the creator or an admin
        User currentUser = userService.findById(userId);
        if (!currentUser.getIsAdmin() && !group.getCreator().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to update this group");
        }

        if (groupRequest.getName() != null) {
            group.setName(groupRequest.getName());
        }

        if (groupRequest.getIsPrivate() != null) {
            group.setIsPrivate(groupRequest.getIsPrivate());
        }

        return groupRepository.save(group);
    }

    /**
     * Delete a group
     * @param id ID of group to delete
     * @param userId ID of user deleting the group
     */
    @Transactional
    @CacheEvict(value = {"groups_", "groupStats"}, allEntries = true)
    public void deleteGroup(Long id, Long userId) {
        Group group = findById(id);

        // Check if current user is the creator or an admin
        User currentUser = userService.findById(userId);
        if (!currentUser.getIsAdmin() && !group.getCreator().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to delete this group");
        }

        groupRepository.delete(group);
    }

    /**
     * Add a user to a group
     * @param groupId ID of group
     * @param userId ID of user to add
     * @param currentUserId ID of current user
     * @return Updated group
     */
    @Transactional
    @CacheEvict(value = {"groups_", "groupStats"}, allEntries = true)
    public Group addUserToGroup(Long groupId, Long userId, Long currentUserId) {
        Group group = findById(groupId);
        User user = userService.findById(userId);

        // Check if current user is the creator or an admin
        User currentUser = userService.findById(currentUserId);
        if (!currentUser.getIsAdmin() && !group.getCreator().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the group creator can add members");
        }

        // Add user to group
        group.getMembers().add(user);

        return groupRepository.save(group);
    }

    /**
     * Remove a user from a group
     * @param groupId ID of group
     * @param userId ID of user to remove
     * @param currentUserId ID of current user
     * @return Updated group
     */
    @Transactional
    @CacheEvict(value = {"groups_", "groupStats"}, allEntries = true)
    public Group removeUserFromGroup(Long groupId, Long userId, Long currentUserId) {
        Group group = findById(groupId);
        User user = userService.findById(userId);

        // Check if current user is the creator or an admin
        User currentUser = userService.findById(currentUserId);
        if (!currentUser.getIsAdmin() && !group.getCreator().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the group creator can remove members");
        }

        // Remove user from group
        group.getMembers().remove(user);

        return groupRepository.save(group);
    }

    /**
     * Get all groups
     * @param searchTerm Search term for filtering
     * @param pageable Pagination parameters
     * @return Page of groups
     */
    @Cacheable(value = "groups_", key = "'all_search_' + #searchTerm + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Group> getAllGroups(String searchTerm, Pageable pageable) {
        if (searchTerm != null && !searchTerm.isEmpty()) {
            return groupRepository.searchGroups(searchTerm, pageable);
        }
        return groupRepository.findAll(pageable);
    }

    /**
     * Get visible groups for a user
     * @param userId ID of user
     * @param pageable Pagination parameters
     * @return Page of groups visible to the user
     */
    public Page<Group> getVisibleGroups(Long userId, Pageable pageable) {
        return groupRepository.findVisibleGroups(userId, pageable);
    }

    /**
     * Get groups where user is a member
     * @param userId ID of user
     * @return List of groups
     */
    @Cacheable(value = "groups_", key = "'member_' + #userId")
    public List<Group> getGroupsByMemberId(Long userId) {
        return groupRepository.findGroupsByMemberId(userId);
    }

    /**
     * Get groups created by a user
     * @param userId ID of user
     * @return List of groups
     */
    @Cacheable(value = "groups_", key = "'creator_' + #userId")
    public List<Group> getGroupsByCreatorId(Long userId) {
        return groupRepository.findByCreatorId(userId);
    }

    /**
     * Get groups ordered by member count
     * @param pageable Pagination parameters
     * @return Page of groups with member count
     */
    @Cacheable(value = "groupStats", key = "'memberCount_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Object[]> getGroupsOrderedByMemberCount(Pageable pageable) {
        return groupRepository.findGroupsOrderedByMemberCount(pageable);
    }

    /**
     * Get groups ordered by post count
     * @param pageable Pagination parameters
     * @return Page of groups with post count
     */
    @Cacheable(value = "groupStats", key = "'postCount_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Object[]> getGroupsOrderedByPostCount(Pageable pageable) {
        return groupRepository.findGroupsOrderedByPostCount(pageable);
    }
}
