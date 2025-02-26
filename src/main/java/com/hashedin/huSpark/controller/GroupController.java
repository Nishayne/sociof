package com.hashedin.huSpark.controller;

import com.hashedin.huSpark.dto.GroupDto;
import com.hashedin.huSpark.dto.GroupRequest;
import com.hashedin.huSpark.entity.Group;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.GroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for group operations
 */
@RestController
@RequestMapping("/api/groups")
@Api(tags = "Groups")
public class GroupController {

    Logger log = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private GroupService groupService;

    /**
     * Create a new group
     * @param groupRequest Group creation request
     * @param currentUser Current authenticated user
     * @return Created group
     */
    @PostMapping
    @ApiOperation("Create a new group")
    public ResponseEntity<Group> createGroup(
            @Valid @RequestBody GroupRequest groupRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: createGroup : UserID: " + currentUser.getId());

        Group group = groupService.createGroup(groupRequest, currentUser.getId());
        return ResponseEntity.ok(group);
    }

    /**
     * Get a group by ID
     * @param id Group ID
     * @return Group
     */
    @GetMapping("/{id}")
    @ApiOperation("Get a group by ID")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        log.info("GroupController: getGroupId: " + id);

        Group group = groupService.findById(id);
        return ResponseEntity.ok(group);
    }

    /**
     * Update a group
     * @param id Group ID
     * @param groupRequest Group update request
     * @param currentUser Current authenticated user
     * @return Updated group
     */
    @PutMapping("/{id}")
    @ApiOperation("Update a group")
    public ResponseEntity<Group> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody GroupRequest groupRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: Update groupId: " + id);

        Group updatedGroup = groupService.updateGroup(id, groupRequest, currentUser.getId());
        return ResponseEntity.ok(updatedGroup);
    }

    /**
     * Delete a group
     * @param id Group ID
     * @param currentUser Current authenticated user
     * @return Deleted group
     */
    @DeleteMapping("/{id}")
    @ApiOperation("Delete a group")
    public ResponseEntity<?> deleteGroup(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: Delete groupId: " + id);

        groupService.deleteGroup(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Add a user to a group
     * @param groupId Group ID
     * @param userId User ID to add
     * @param currentUser Current authenticated user
     * @return Updated group
     */
    @PostMapping("/{groupId}/members/{userId}")
    @ApiOperation("Add a user to a group")
    public ResponseEntity<Group> addUserToGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: addUser groupId: " + groupId);

        Group group = groupService.addUserToGroup(groupId, userId, currentUser.getId());
        return ResponseEntity.ok(group);
    }

    /**
     * Remove a user from a group
     * @param groupId Group ID
     * @param userId User ID to remove
     * @param currentUser Current authenticated user
     * @return Updated group
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    @ApiOperation("Remove a user from a group")
    public ResponseEntity<Group> removeUserFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: removeUser groupId: " + groupId);

        Group group = groupService.removeUserFromGroup(groupId, userId, currentUser.getId());
        return ResponseEntity.ok(group);
    }

    /**
     * Get all groups
     * @param searchTerm Search term for filtering
     * @param pageable Pagination parameters
     * @return Page of groups
     */
    @GetMapping
    @ApiOperation("Get all groups")
    public ResponseEntity<Page<Group>> getAllGroups(
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        log.info("GroupController: getAllGroups searchTerm: " + searchTerm);

        Page<Group> groups = groupService.getAllGroups(searchTerm, pageable);
        return ResponseEntity.ok(groups);
    }

    /**
     * Get visible groups for current user
     * @param currentUser Current authenticated user
     * @param pageable Pagination parameters
     * @return Page of groups
     */
    @GetMapping("/visible")
    @ApiOperation("Get visible groups for current user")
    public ResponseEntity<Page<Group>> getVisibleGroups(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        log.info("GroupController: getAllGroupsVisible: UserId: " + currentUser.getId());

        Page<Group> groups = groupService.getVisibleGroups(currentUser.getId(), pageable);
        return ResponseEntity.ok(groups);
    }

    /**
     * Get groups where user is a member
     * @param currentUser Current authenticated user
     * @return List of groups
     */
    @GetMapping("/my-groups")
    @ApiOperation("Get groups where user is a member")
    public ResponseEntity<List<Group>> getMyGroups(@CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: getMyGroups: UserId: " + currentUser.getId());

        List<Group> groups = groupService.getGroupsByMemberId(currentUser.getId());
        return ResponseEntity.ok(groups);
    }

    /**
     * Get groups created by current user
     * @param currentUser Current authenticated user
     * @return List of groups
     */
    @GetMapping("/created")
    @ApiOperation("Get groups created by current user")
    public ResponseEntity<List<Group>> getCreatedGroups(@CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: getGroupsCreated: UserId: " + currentUser.getId());

        List<Group> groups = groupService.getGroupsByCreatorId(currentUser.getId());
        return ResponseEntity.ok(groups);
    }

    /**
     * Get groups ordered by member count
     * @param pageable Pagination parameters
     * @return Page of groups with member count
     */
    @GetMapping("/stats/members")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get groups ordered by member count (Admin only)")
    public ResponseEntity<Page<Object[]>> getGroupsOrderedByMemberCount(Pageable pageable) {
        log.info("GroupController: getGroupsOrderedByMemberCount");

        Page<Object[]> groups = groupService.getGroupsOrderedByMemberCount(pageable);
        return ResponseEntity.ok(groups);
    }

    /**
     * Get groups ordered by post count
     * @param pageable Pagination parameters
     * @return Page of groups with post count
     */
    @GetMapping("/stats/posts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get groups ordered by post count (Admin only)")
    public ResponseEntity<Page<Object[]>> getGroupsOrderedByPostCount(Pageable pageable) {
        log.info("GroupController: getGroupsOrderedByPostCount");

        Page<Object[]> groups = groupService.getGroupsOrderedByPostCount(pageable);
        return ResponseEntity.ok(groups);
    }
}
