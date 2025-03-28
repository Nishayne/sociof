package com.hashedin.huSpark.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hashedin.huSpark.dto.GroupMemberCountDTO;
import com.hashedin.huSpark.dto.GroupPostCountDTO;
import com.hashedin.huSpark.dto.GroupRequest;
import com.hashedin.huSpark.entity.Group;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.exception.ResourceAlreadyExistsException;
import com.hashedin.huSpark.exception.ResourceNotFoundException;
import com.hashedin.huSpark.exception.UnauthorizedException;
import com.hashedin.huSpark.repository.GroupRepository;

/**
 * Service for group-related operations.
 */
@Service
public class GroupService {

    private final Logger log = LoggerFactory.getLogger(GroupService.class);

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserService userService;

    /**
     * Create a new group.
     * @param groupRequest Group creation request
     * @param creatorId ID of user creating the group
     * @return Created group
     */
    @Transactional
    @CacheEvict(value = {"groups_", "groupStats"}, allEntries = true)
    public Group createGroup(GroupRequest groupRequest, Long creatorId) {
        log.info("GroupService: createGroup: CreatorId: {}", creatorId);
        User creator = userService.findById(creatorId);

        //check if same group name already exists
        //if yes throw IllegalArgumentException
        // Check if a group with the same name already exists
        if (groupRepository.existsByName(groupRequest.getName())) {
            log.warn("Group with name '{}' already exists.", groupRequest.getName());
            throw new ResourceAlreadyExistsException("Group with name '" + groupRequest.getName() + "' already exists.");
        }
        Group group = Group.builder()
                .name(groupRequest.getName())
                .isPrivate(groupRequest.getIsPrivate())
                .creator(creator)
                .build();
        Group retVal = null;
        try {
            // Add creator as a member
            group.getMembers().add(creator);
            retVal = groupRepository.saveAndFlush(group); // Changed from save to saveAndFlush
            log.info("Group created successfully: GroupId: {}", retVal.getId());
            return retVal;
        } catch (Exception e) {
            log.error("Failed to create group: {}", e.getMessage());
            System.err.println("createGroup exception  " + e.toString());
        }
        return retVal;
    }

    /**
     * Find a group by ID.
     * @param id ID of group to find
     * @return Group
     * @throws ResourceNotFoundException if group is not found
     */
    public Group findById(Long id) {
        log.info("GroupService: findById: GroupId: {}", id);
        return groupRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Group not found with id: {}", id);
                    return new ResourceNotFoundException("Group not found with id: " + id);
                });
    }

    /**
     * Update a group.
     * @param id ID of group to update
     * @param groupRequest Group update request
     * @param userId ID of user updating the group
     * @return Updated group
     */
    @Transactional
    @CacheEvict(value = {"groups_", "groupStats"}, allEntries = true)
    public Group updateGroup(Long id, GroupRequest groupRequest, Long userId) {
        log.info("GroupService: updateGroup: GroupId: {}, UserId: {}", id, userId);
        Group group = findById(id);

        // Check if current user is the creator or an admin
        User currentUser = userService.findById(userId);
        if (!currentUser.getIsAdmin() && !group.getCreator().getId().equals(userId)) {
            log.warn("User {} does not have permission to update group {}.", userId, id);
            throw new UnauthorizedException("You do not have permission to update this group");
        }

        if (groupRequest.getName() != null) {
            group.setName(groupRequest.getName());
        }

        if (groupRequest.getIsPrivate() != null) {
            group.setIsPrivate(groupRequest.getIsPrivate());
        }

        Group updatedGroup = groupRepository.saveAndFlush(group); // Changed from save to saveAndFlush
        log.info("Group updated successfully: GroupId: {}", updatedGroup.getId());
        return updatedGroup;
    }

    /**
     * Delete a group.
     * @param id ID of group to delete
     * @param userId ID of user deleting the group
     */
    @Transactional
    @CacheEvict(value = {"groups_", "groupStats"}, allEntries = true)
    public void deleteGroup(Long id, Long userId) {
        log.info("GroupService: deleteGroup: GroupId: {}, UserId: {}", id, userId);
        Group group = findById(id);

        // Check if current user is the creator or an admin
        User currentUser = userService.findById(userId);
        if (!currentUser.getIsAdmin() && !group.getCreator().getId().equals(userId)) {
            log.warn("User {} does not have permission to delete group {}.", userId, id);
            throw new UnauthorizedException("You do not have permission to delete this group");
        }

        groupRepository.delete(group);
        log.info("Group deleted successfully: GroupId: {}", id);
    }

    /**
     * Add a user to a group.
     * @param groupId ID of group
     * @param userId ID of user to add
     * @param currentUserId ID of current user
     * @return Updated group
     */
    @Transactional
    @CacheEvict(value = {"groups_", "groupStats"}, allEntries = true)
    public Group addUserToGroup(Long groupId, Long userId, Long currentUserId) {
        log.info("GroupService: addUserToGroup: GroupId: {}, UserId: {}, CurrentUserId: {}", groupId, userId, currentUserId);
        Group group = findById(groupId);
        User user = userService.findById(userId);

        // Check if current user is the creator or an admin
        User currentUser = userService.findById(currentUserId);
        if (!currentUser.getIsAdmin() && !group.getCreator().getId().equals(currentUserId)) {
            log.warn("User {} does not have permission to add members to group {}.", currentUserId, groupId);
            throw new UnauthorizedException("Only the group creator can add members");
        }

        // Add user to group
        group.getMembers().add(user);

        Group updatedGroup = groupRepository.saveAndFlush(group); // Changed from save to saveAndFlush
        log.info("User {} added to group {} successfully.", userId, groupId);
        return updatedGroup;
    }

    /**
     * Remove a user from a group.
     * @param groupId ID of group
     * @param userId ID of user to remove
     * @param currentUserId ID of current user
     * @return Updated group
     */
    @Transactional
    @CacheEvict(value = {"groups_", "groupStats"}, allEntries = true)
    public Group removeUserFromGroup(Long groupId, Long userId, Long currentUserId) {
        log.info("GroupService: removeUserFromGroup: GroupId: {}, UserId: {}, CurrentUserId: {}", groupId, userId, currentUserId);
        Group group = findById(groupId);
        User user = userService.findById(userId);

        // Check if current user is the creator or an admin
        User currentUser = userService.findById(currentUserId);
        if (!currentUser.getIsAdmin() && !group.getCreator().getId().equals(currentUserId)) {
            log.warn("User {} does not have permission to remove members from group {}.", currentUserId, groupId);
            throw new UnauthorizedException("Only the group creator can remove members");
        }

        // Remove user from group
        group.getMembers().remove(user);

        Group updatedGroup = groupRepository.saveAndFlush(group); // Changed from save to saveAndFlush
        log.info("User {} removed from group {} successfully.", userId, groupId);
        return updatedGroup;
    }

    /**
     * Get all groups.
     * @param searchTerm Search term for filtering
     * @param pageable Pagination parameters
     * @return Page of groups
     */
    @Cacheable(value = "groups_", key = "'all_search_' + #searchTerm + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Group> getAllGroups(String searchTerm, Pageable pageable) {
        log.info("GroupService: getAllGroups: SearchTerm: {}, Pageable: {}", searchTerm, pageable);
        Page<Group> groups;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            groups = groupRepository.searchGroups(searchTerm, pageable);
        } else {
            groups = groupRepository.findAll(pageable);
        }
        log.info("Found {} groups.", groups.getTotalElements());
        return groups;
    }
/**
     * Get visible groups for a user.
     * @param userId ID of user
     * @param pageable Pagination parameters
     * @return Page of groups visible to the user
     */
    public Page<Group> getVisibleGroups(Long userId, Pageable pageable) {
        log.info("GroupService: getVisibleGroups: UserId: {}, Pageable: {}", userId, pageable);
        Page<Group> visibleGroups = groupRepository.findVisibleGroups(userId, pageable);
        log.info("Found {} visible groups for user {}.", visibleGroups.getTotalElements(), userId);
        return visibleGroups;
    }

    /**
     * Get groups where user is a member.
     * @param userId ID of user
     * @return List of groups
     */
    @Cacheable(value = "groups_", key = "'member_' + #userId")
    public List<Group> getGroupsByMemberId(Long userId) {
        log.info("GroupService: getGroupsByMemberId: UserId: {}", userId);
        List<Group> memberGroups = groupRepository.findGroupsByMemberId(userId);
        log.info("Found {} groups where user {} is a member.", memberGroups.size(), userId);
        return memberGroups;
    }

    /**
     * Get groups created by a user.
     * @param userId ID of user
     * @return List of groups
     */
    @Cacheable(value = "groups_", key = "'creator_' + #userId")
    public List<Group> getGroupsByCreatorId(Long userId) {
        log.info("GroupService: getGroupsByCreatorId: UserId: {}", userId);
        List<Group> createdGroups = groupRepository.findByCreatorId(userId);
        log.info("Found {} groups created by user {}.", createdGroups.size(), userId);
        return createdGroups;
    }

    /**
     * Get groups ordered by member count.
     * @param pageable Pagination parameters
     * @param currentUserId loginuser parameter
     * @return Page of groups with member count
     */
     @Cacheable(value = "groupStats", key = "'memberCount_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<GroupMemberCountDTO> getGroupsOrderedByMemberCount(Pageable pageable, Long currentUserId) {
        log.info("GroupService: getGroupsOrderedByMemberCount: Pageable: {}", pageable);
        User currentUser = userService.findById(currentUserId); 
        if (!currentUser.getIsAdmin())
        {
            log.warn("Unauthorized access: User {} does not have permission to getGroupsOrderedByMemberCount.", currentUserId);
            throw new UnauthorizedException("Unauthorized access: You do not have permission to getGroupsOrderedByMemberCount");
        } 
        Page<GroupMemberCountDTO> orderedGroups = groupRepository.findGroupsOrderedByMemberCount(pageable);
        log.info("Found {} groups ordered by member count.", orderedGroups.getTotalElements());
        return orderedGroups;
    }

    /**
     * Get groups ordered by post count.
     * @param pageable Pagination parameters
     * @param currentUserId loginuser parameter
     * @return Page of groups with post count
     */
    @Cacheable(value = "groupStats", key = "'postCount_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<GroupPostCountDTO> getGroupsOrderedByPostCount(Pageable pageable, Long currentUserId) {
        log.info("GroupService: getGroupsOrderedByPostCount: Pageable: {}", pageable);
        User currentUser = userService.findById(currentUserId); 
        if (!currentUser.getIsAdmin())
        {
            log.warn("Unauthorized access: User {} does not have permission to getGroupsOrderedByPostCount.", currentUserId);
            throw new UnauthorizedException("Unauthorized access: You do not have permission to getGroupsOrderedByPostCount");
        } 
        Page<GroupPostCountDTO> orderedGroups = groupRepository.findGroupsOrderedByPostCount(pageable);
        log.info("Found {} groups ordered by post count.", orderedGroups.getTotalElements());
        return orderedGroups;
    }
}
