package com.hashedin.huSpark.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hashedin.huSpark.dto.GroupDto;
import com.hashedin.huSpark.dto.GroupMemberCountDTO;
import com.hashedin.huSpark.dto.GroupPostCountDTO;
import com.hashedin.huSpark.dto.GroupRequest; // Import GroupDto
import com.hashedin.huSpark.entity.Group;
import com.hashedin.huSpark.security.CurrentUser;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.GroupService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;

/**
 * Controller for group operations.
 * Handles group creation, retrieval, updating, deletion, and membership management.
 */
@RestController
@RequestMapping("/api/groups")
@Api(tags = "Groups")
public class GroupController {

    private final Logger log = LoggerFactory.getLogger(GroupController.class);
    private final GroupService groupService;
    private final ModelMapper modelMapper = new ModelMapper(); // Initialize ModelMapper for DTO conversion

    /**
     * Constructor for GroupController.
     * @param groupService Service for group operations.
     */
    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Creates a new group.
     * @param groupRequest Group creation request (contains group name, privacy settings).
     * @param currentUser Current authenticated user (creator of the group).
     * @return ResponseEntity containing the created GroupDto or an error response.
     */
    @PostMapping
    @ApiOperation("Create a new group")
    public ResponseEntity<GroupDto> createGroup(
            @Valid @RequestBody GroupRequest groupRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: createGroup : UserID: " + currentUser.getId());

        try {
            Group group = groupService.createGroup(groupRequest, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(group, GroupDto.class)); // Return GroupDto
        } catch (IllegalArgumentException e) {
            log.warn("Group creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Group creation failed: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves a group by its ID.
     * @param id Group ID.
     * @return ResponseEntity containing the GroupDto or an error response.
     */
    @GetMapping("/{id}")
    @ApiOperation("Get a group by ID")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable Long id) {
        log.info("GroupController: getGroupId: " + id);

        try {
            Group group = groupService.findById(id);
            return ResponseEntity.ok(modelMapper.map(group, GroupDto.class)); // Return GroupDto
        } catch (Exception e) {
            log.error("Failed to get group: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Updates a group.
     * @param id Group ID.
     * @param groupRequest Group update request (contains updated group data).
     * @param currentUser Current authenticated user (must be creator or admin).
     * @return ResponseEntity containing the updated GroupDto or an error response.
     */
    @PutMapping("/{id}")
    @ApiOperation("Update a group")
    public ResponseEntity<GroupDto> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody GroupRequest groupRequest,
            @CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: Update groupId: " + id);

        try {
            Group updatedGroup = groupService.updateGroup(id, groupRequest, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(updatedGroup, GroupDto.class)); // Return GroupDto
        } catch (IllegalArgumentException e) {
            log.warn("Group update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Group update failed: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Deletes a group.
     * @param id Group ID.
     * @param currentUser Current authenticated user (must be creator or admin).
     * @return ResponseEntity indicating successful deletion or an error response.
     */
    @DeleteMapping("/{id}")
    @ApiOperation("Delete a group")
    public ResponseEntity<?> deleteGroup(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: Delete groupId: " + id);

        try {
            groupService.deleteGroup(id, currentUser.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Group deletion failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Group deletion failed: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Adds a user to a group.
     * @param groupId Group ID.
     * @param userId User ID to add.
     * @param currentUser Current authenticated user (must be creator or admin).
     * @return ResponseEntity containing the updated GroupDto or an error response.
     */
    @PostMapping("/{groupId}/members/{userId}")
    @ApiOperation("Add a user to a group")
    public ResponseEntity<GroupDto> addUserToGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: addUser groupId: " + groupId);

        try {
            Group group = groupService.addUserToGroup(groupId, userId, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(group, GroupDto.class));
        } catch (IllegalArgumentException e) {
            log.warn("Adding user to group failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Adding user to group failed: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Removes a user from a group.
     * @param groupId Group ID.
     * @param userId User ID to remove.
     * @param currentUser Current authenticated user (must be creator or admin).
     * @return ResponseEntity containing the updated GroupDto or an error response.
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    @ApiOperation("Remove a user from a group")
    public ResponseEntity<GroupDto> removeUserFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: removeUser groupId: " + groupId);

        try {
            Group group = groupService.removeUserFromGroup(groupId, userId, currentUser.getId());
            return ResponseEntity.ok(modelMapper.map(group, GroupDto.class));
        } catch (IllegalArgumentException e) {
            log.warn("Removing user from group failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Removing user from group failed: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves all groups (with optional search term and pagination).
     * @param searchTerm Search term for filtering groups.
     * @param pageable Pagination parameters.
     * @return ResponseEntity containing a page of GroupDto or an error response.
     */
    @GetMapping
    @ApiOperation("Get all groups")
    public ResponseEntity<Page<GroupDto>> getAllGroups(
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        log.info("GroupController: getAllGroups searchTerm: " + searchTerm);

        try {
            Page<Group> groups = groupService.getAllGroups(searchTerm, pageable);
            Page<GroupDto> groupDtos = groups.map(group -> modelMapper.map(group, GroupDto.class));
            return ResponseEntity.ok(groupDtos);
        } catch (Exception e) {
            log.error("Failed to get all groups: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves visible groups for the current user (with pagination).
     * @param currentUser Current authenticated user.
     * @param pageable Pagination parameters.
     * @return ResponseEntity containing a page of GroupDto or an error response.
     */
    @GetMapping("/visible")
    @ApiOperation("Get visible groups for current user")
    public ResponseEntity<Page<GroupDto>> getVisibleGroups(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        log.info("GroupController: getAllGroupsVisible: UserId: " + currentUser.getId());

        try {
            Page<Group> groups = groupService.getVisibleGroups(currentUser.getId(), pageable);
            Page<GroupDto> groupDtos = groups.map(group -> modelMapper.map(group, GroupDto.class));
            return ResponseEntity.ok(groupDtos);
        } catch (Exception e) {
            log.error("Failed to get visible groups: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves groups where the current user is a member.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing a list of GroupDto or an error response.
     */
    @GetMapping("/my-groups")
    @ApiOperation("Get groups where user is a member")
    public ResponseEntity<List<GroupDto>> getMyGroups(@CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: getMyGroups: UserId: " + currentUser.getId());

        try {
            List<Group> groups = groupService.getGroupsByMemberId(currentUser.getId());
            List<GroupDto> groupDtos = groups.stream()
                                            .map(group -> modelMapper.map(group, GroupDto.class))
                                            .collect(Collectors.toList());
            return ResponseEntity.ok(groupDtos);
        } catch (Exception e) {
            log.error("Failed to get user's groups: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves groups created by the current user.
     * @param currentUser Current authenticated user.
     * @return ResponseEntity containing a list of GroupDto or an error response.
     */
    @GetMapping("/created")
    @ApiOperation("Get groups created by current user")
    public ResponseEntity<List<GroupDto>> getCreatedGroups(@CurrentUser UserPrincipal currentUser) {
        log.info("GroupController: getGroupsCreated: UserId: " + currentUser.getId());

        try {
            List<Group> groups = groupService.getGroupsByCreatorId(currentUser.getId());
            List<GroupDto> groupDtos = groups.stream()
                                            .map(group -> modelMapper.map(group, GroupDto.class))
                                            .collect(Collectors.toList());
            return ResponseEntity.ok(groupDtos);
        } catch (Exception e) {
            log.error("Failed to get created groups: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves groups ordered by member count (Admin only).
     * @param pageable Pagination parameters.
     * @return ResponseEntity containing a page of Object[] or an error response.
     */
    @GetMapping("/stats/members")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get groups ordered by member count (Admin only)")
    public ResponseEntity<Page<GroupMemberCountDTO>> getGroupsOrderedByMemberCount(Pageable pageable) {
        log.info("GroupController: getGroupsOrderedByMemberCount");

        try {
            Page<GroupMemberCountDTO> groups = groupService.getGroupsOrderedByMemberCount(pageable);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            log.error("Failed to get groups by member count: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves groups ordered by post count (Admin only).
     * @param pageable Pagination parameters.
     * @return ResponseEntity containing a page of groups with post count or an error response.
     */
    @GetMapping("/stats/posts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation("Get groups ordered by post count (Admin only)")
    public ResponseEntity<Page<GroupPostCountDTO>> getGroupsOrderedByPostCount(Pageable pageable) {
        log.info("GroupController: getGroupsOrderedByPostCount");

        try {
            Page<GroupPostCountDTO> groups = groupService.getGroupsOrderedByPostCount(pageable);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            log.error("Failed to get groups by post count: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
