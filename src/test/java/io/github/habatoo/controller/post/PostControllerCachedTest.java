package io.github.habatoo.controller.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.habatoo.controller.PostController;
import io.github.habatoo.dto.request.PostCreateRequest;
import io.github.habatoo.dto.request.PostRequest;
import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.handler.GlobalExceptionHandler;
import io.github.habatoo.service.PostService;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты для PostController с максимальным кешированием MockMvc
 * Использует Standalone Setup с @BeforeAll для однократной инициализации
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Тесты unit уровня методов контроллера PostController с использованием Cached MockMvc.")
class PostControllerCachedTest {

    private MockMvc mockMvc;
    private PostService postService;
    private ObjectMapper objectMapper;

    private PostResponse mockPost1;
    private PostResponse mockPost2;
    private PostListResponse mockPostListResponse;
    private PostCreateRequest mockCreateRequest;
    private PostRequest mockUpdateRequest;

    @BeforeAll
    void setUpAll() {
        postService = mock(PostService.class);
        PostController postController = new PostController(postService);
        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        initializeTestData();
    }

    /**
     * Инициализация тестовых данных
     */
    private void initializeTestData() {
        mockPost1 = new PostResponse(1L, "Первый пост", "Текст первого поста",
                Arrays.asList("технологии", "java"), 10, 5);
        mockPost2 = new PostResponse(2L, "Второй пост", "Текст второго поста",
                Arrays.asList("spring", "тестирование"), 5, 2);

        List<PostResponse> posts = Arrays.asList(mockPost1, mockPost2);
        mockPostListResponse = new PostListResponse(posts, true, false, 5);
        mockCreateRequest = new PostCreateRequest("Новый пост", "Текст нового поста",
                Arrays.asList("новости", "блог"));
        mockUpdateRequest = new PostRequest(1L, "Обновленный пост", "Обновленный текст",
                Arrays.asList("обновление", "технологии"));
    }

    @BeforeEach
    void setUp() {
        reset(postService);
    }

    /**
     * Тест успешного получения списка постов с пагинацией
     */
    @Test
    @DisplayName("GET /api/posts - должен вернуть список постов с пагинацией")
    void getPosts_WithValidParams_ShouldReturnPostList() throws Exception {
        String search = "технологии";
        int pageNumber = 1;
        int pageSize = 10;

        when(postService.getPosts(search, pageNumber, pageSize))
                .thenReturn(mockPostListResponse);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts")
                        .param("search", search)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        PostListResponse response = objectMapper.readValue(responseContent, PostListResponse.class);

        assertEquals(2, response.posts().size());
        assertEquals("Первый пост", response.posts().get(0).title());
        assertEquals("Второй пост", response.posts().get(1).title());
        assertTrue(response.hasPrev());

        verify(postService, times(1)).getPosts(search, pageNumber, pageSize);
    }

    /**
     * Тест получения пустого списка постов
     */
    @Test
    @DisplayName("GET /api/posts - должен вернуть пустой список")
    void getPosts_WithNoPosts_ShouldReturnEmptyList() throws Exception {
        String search = "несуществующий запрос";
        int pageNumber = 1;
        int pageSize = 10;

        PostListResponse emptyResponse = new PostListResponse(Collections.emptyList(), false, false, 0);
        when(postService.getPosts(search, pageNumber, pageSize))
                .thenReturn(emptyResponse);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts")
                        .param("search", search)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        PostListResponse response = objectMapper.readValue(responseContent, PostListResponse.class);

        assertTrue(response.posts().isEmpty());
        verify(postService, times(1)).getPosts(search, pageNumber, pageSize);
    }

    /**
     * Тест успешного получения поста по ID
     */
    @Test
    @DisplayName("GET /api/posts/{id} - должен вернуть пост")
    void getPostById_WithValidId_ShouldReturnPost() throws Exception {
        Long postId = 1L;
        when(postService.getPostById(postId))
                .thenReturn(Optional.of(mockPost1));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        PostResponse response = objectMapper.readValue(responseContent, PostResponse.class);

        assertEquals(mockPost1.id(), response.id());
        assertEquals(mockPost1.title(), response.title());
        assertEquals(mockPost1.text(), response.text());
        assertEquals(2, response.tags().size());

        verify(postService, times(1)).getPostById(postId);
    }

    /**
     * Тест получения несуществующего поста
     */
    @Test
    @DisplayName("GET /api/posts/{id} - должен вернуть 404 для несуществующего поста")
    void getPostById_WithNonExistentPost_ShouldReturnNotFound() throws Exception {
        Long postId = 999L;
        when(postService.getPostById(postId))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(postService, times(1)).getPostById(postId);
    }

    /**
     * Тест успешного создания поста
     */
    @Test
    @DisplayName("POST /api/posts - должен создать пост")
    void createPost_WithValidRequest_ShouldReturnCreated() throws Exception {
        when(postService.createPost(any(PostCreateRequest.class)))
                .thenReturn(mockPost1);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCreateRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        PostResponse response = objectMapper.readValue(responseContent, PostResponse.class);

        assertEquals(mockPost1.id(), response.id());
        assertEquals(mockPost1.title(), response.title());
        assertEquals(mockPost1.text(), response.text());

        verify(postService, times(1)).createPost(any(PostCreateRequest.class));
    }

    /**
     * Тест создания поста с невалидными данными
     */
    @Test
    @DisplayName("POST /api/posts - должен вернуть ошибку для невалидных данных")
    void createPost_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        when(postService.createPost(any(PostCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid post data"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCreateRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(postService, times(1)).createPost(any(PostCreateRequest.class));
    }

    /**
     * Тест успешного обновления поста
     */
    @Test
    @DisplayName("PUT /api/posts/{id} - должен обновить пост")
    void updatePost_WithValidRequest_ShouldReturnUpdatedPost() throws Exception {
        Long postId = 1L;
        PostResponse updatedPost = new PostResponse(postId, "Обновленный пост",
                "Обновленный текст", Arrays.asList("обновление"), 10, 5);

        when(postService.updatePost(any(PostRequest.class)))
                .thenReturn(updatedPost);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUpdateRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        PostResponse response = objectMapper.readValue(responseContent, PostResponse.class);

        assertEquals(postId, response.id());
        assertEquals("Обновленный пост", response.title());
        assertEquals("Обновленный текст", response.text());

        verify(postService, times(1)).updatePost(any(PostRequest.class));
    }

    /**
     * Тест обновления несуществующего поста
     */
    @Test
    @DisplayName("PUT /api/posts/{id} - должен вернуть ошибку для несуществующего поста")
    void updatePost_WithNonExistentPost_ShouldThrowException() throws Exception {
        Long postId = 999L;
        when(postService.updatePost(any(PostRequest.class)))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException("Post not found", 1));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUpdateRequest)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(postService, times(1)).updatePost(any(PostRequest.class));
    }

    /**
     * Тест успешного удаления поста
     */
    @Test
    @DisplayName("DELETE /api/posts/{id} - должен удалить пост")
    void deletePost_WithValidId_ShouldReturnOk() throws Exception {
        Long postId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(postService, times(1)).deletePost(postId);
    }

    /**
     * Тест удаления несуществующего поста
     */
    @Test
    @DisplayName("DELETE /api/posts/{id} - должен вернуть ошибку для несуществующего поста")
    void deletePost_WithNonExistentPost_ShouldThrowException() throws Exception {
        Long postId = 999L;
        doThrow(new org.springframework.dao.EmptyResultDataAccessException("Post not found", 1))
                .when(postService).deletePost(postId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(postService, times(1)).deletePost(postId);
    }

    /**
     * Тест успешного увеличения лайков
     */
    @Test
    @DisplayName("POST /api/posts/{id}/likes - должен увеличить лайки")
    void incrementLikes_WithValidId_ShouldReturnLikesCount() throws Exception {
        Long postId = 1L;
        int expectedLikes = 11;
        when(postService.incrementLikes(postId))
                .thenReturn(expectedLikes);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/{id}/likes", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertEquals(String.valueOf(expectedLikes), responseContent);

        verify(postService, times(1)).incrementLikes(postId);
    }

    /**
     * Тест увеличения лайков для несуществующего поста
     */
    @Test
    @DisplayName("POST /api/posts/{id}/likes - должен вернуть ошибку для несуществующего поста")
    void incrementLikes_WithNonExistentPost_ShouldThrowException() throws Exception {
        Long postId = 999L;
        when(postService.incrementLikes(postId))
                .thenThrow(new org.springframework.dao.EmptyResultDataAccessException("Post not found", 1));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/{id}/likes", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(postService, times(1)).incrementLikes(postId);
    }

    /**
     * Тест обработки ошибки доступа к данным
     */
    @Test
    @DisplayName("GET /api/posts - должен вернуть 500 при ошибке БД")
    void getPosts_WithDataAccessError_ShouldReturnInternalServerError() throws Exception {
        String search = "технологии";
        int pageNumber = 1;
        int pageSize = 10;

        when(postService.getPosts(search, pageNumber, pageSize))
                .thenThrow(new org.springframework.dao.DataAccessException("Database error") {
                });

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts")
                        .param("search", search)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        verify(postService, times(1)).getPosts(search, pageNumber, pageSize);
    }

    /**
     * Тест с разными параметрами пагинации
     */
    @Test
    @DisplayName("GET /api/posts - должен работать с разными параметрами пагинации")
    void getPosts_WithDifferentPaginationParams_ShouldWorkCorrectly() throws Exception {
        Object[][] testCases = {
                {"запрос1", 1, 10},
                {"java", 2, 5},
                {"spring", 3, 20}
        };

        for (Object[] testCase : testCases) {
            reset(postService);
            String search = (String) testCase[0];
            int pageNumber = (Integer) testCase[1];
            int pageSize = (Integer) testCase[2];

            when(postService.getPosts(search, pageNumber, pageSize))
                    .thenReturn(mockPostListResponse);

            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts")
                            .param("search", search)
                            .param("pageNumber", String.valueOf(pageNumber))
                            .param("pageSize", String.valueOf(pageSize))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(postService, times(1)).getPosts(search, pageNumber, pageSize);
        }
    }
}
