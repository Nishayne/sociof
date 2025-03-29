package com.hashedin.huSpark.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashedin.huSpark.dto.GroupDto;
import com.hashedin.huSpark.dto.GroupRequest;
import com.hashedin.huSpark.dto.PostDto;
import com.hashedin.huSpark.dto.PostRequest;
import com.hashedin.huSpark.entity.Group;
import com.hashedin.huSpark.entity.Post;
import com.hashedin.huSpark.entity.Role;
import com.hashedin.huSpark.security.UserPrincipal;
import com.hashedin.huSpark.service.GroupService;
import com.hashedin.huSpark.service.PostService;
import com.hashedin.huSpark.service.ShareService;
import com.hashedin.huSpark.service.UserService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @Mock
    private GroupService groupService;

    @Mock
    private ShareService shareService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PostController postController;

    @InjectMocks
    private GroupController groupController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController, postController).build();
    }

    @Test
    //@WithMockUser(username = "testUser", roles = {"USER"}) // Simulating authentication
    void testCreatePost() throws Exception {
         // Mock Security Context for authenticated user
        UserPrincipal userPrincipal = new UserPrincipal(
                                        1L, 
                                        "testUser@example.com", 
                                        "encodedPasswd", 
                                        false, 
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")) 
                                    );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);
        // Arrange
        com.hashedin.huSpark.entity.User testUser = new com.hashedin.huSpark.entity.User();
        testUser.setId(1L);
        testUser.setEmail("testUser@example.com");
        testUser.setPassword("encodedPasswd");
        testUser.setIsAdmin(false);
        testUser.setIsProfilePrivate(false);
        testUser.setRole(Role.USER);
        testUser.setDateOfBirth(new Date(1222121212L));

        Group group = new Group();
        group.setId(1L);
        group.setName("Test group");
        group.setIsPrivate(false);
        group.setCreator(testUser);

        GroupDto groupDto = new GroupDto();
        groupDto.setId(1L);
        groupDto.setName("Test Group");
        groupDto.setIsPrivate(false);
        groupDto.setCreatorId(testUser.getId());
        groupDto.setMemberCount(1);
        groupDto.setPostCount(0);
        groupDto.setCreatorEmail("testUser@example.com");

        when(groupService.createGroup(any(GroupRequest.class), anyLong())).thenReturn(group);
        when(modelMapper.map(group, GroupDto.class)).thenReturn(groupDto);

        GroupRequest groupRequest = new GroupRequest();
        groupRequest.setName("Test Group");
        groupRequest.setIsPrivate(false);

        Post post = new Post();
        post.setId(1L);
        post.setContent("Test Post");
        post.setFileUrl("http://example.com/file.jpg");
        post.setFileType("image/jpeg");
        post.setGroup(group);

        PostDto postDto = new PostDto();
        postDto.setId(1L);
        postDto.setContent("Test Post");

        when(userService.findById(anyLong())).thenReturn(testUser);
        when(postService.createPost(any(PostRequest.class), anyLong())).thenReturn(post);
        when(modelMapper.map(post, PostDto.class)).thenReturn(postDto);

        PostRequest postRequest = new PostRequest();
        postRequest.setContent("Test Post");
        postRequest.setFileUrl("http://example.com/file.jpg");
        postRequest.setFileType("image/jpeg");
        postRequest.setGroupId(1L);

        System.out.println(objectMapper.writeValueAsString(groupRequest));

        // Act & Assert
        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities())))
                .with(csrf()) // Ensure CSRF protection is handled if needed
                .characterEncoding("UTF-8") 
                .content(objectMapper.writeValueAsString(groupRequest)))
                .andDo(print())  // This prints full request/response details
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test Group"));

        // Verify interactions
        verify(groupService, times(1)).createGroup(any(GroupRequest.class), anyLong());

        System.out.println(objectMapper.writeValueAsString(postRequest));

        // Act & Assert
        /*MvcResult result = mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities())))
                .with(csrf()) // Ensure CSRF protection is handled if needed
                .characterEncoding("UTF-8") 
                .content(objectMapper.writeValueAsString(postRequest)))
                .andDo(print())  // This prints full request/response details in debug console
                .andReturn();  // Capture response

        System.out.println("Response Status: " + result.getResponse().getStatus());
        System.out.println("Response Body: " + result.getResponse().getContentAsString());*/

        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities())))
                .with(csrf()) // Ensure CSRF protection is handled if needed
                .characterEncoding("UTF-8") 
                .content(objectMapper.writeValueAsString(postRequest)))
                .andDo(print())  // This prints full request/response details in debug console
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test Post"));

        // Verify interactions
        verify(postService, times(1)).createPost(any(PostRequest.class), anyLong());
    }

    @Test
    void testGetPostById() throws Exception {
        Long postId = 1L;
        Post post = new Post();
        post.setId(postId);
        post.setContent("Sample Post");
        PostDto postDto = new PostDto();
        postDto.setId(postId);
        postDto.setContent("Sample Post");

        when(postService.findById(postId)).thenReturn(post);
        when(modelMapper.map(post, PostDto.class)).thenReturn(postDto);

        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.content").value("Sample Post"));
    }

    @Test
    void testDeletePost() throws Exception {
        Long postId = 1L;
        doNothing().when(postService).deletePost(eq(postId), anyLong());

        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isOk());

        verify(postService, times(1)).deletePost(eq(postId), anyLong());
    }
}
