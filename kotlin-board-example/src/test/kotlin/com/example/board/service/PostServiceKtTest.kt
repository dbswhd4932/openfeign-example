package com.example.board.service

import com.example.board.dto.PostDtoKt
import com.example.board.entity.PostKt
import com.example.board.repository.PostRepositoryKt
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

/**
 * PostServiceKt 단위 테스트
 *
 * [MockK 사용]
 * - Kotlin 전용 Mocking 라이브러리
 * - mockk(): Mock 객체 생성
 * - every { }: Mock 동작 정의
 * - verify { }: 메서드 호출 검증
 * - slot(): 인자 캡처
 *
 * [Kotlin 테스트 포인트]
 * - lateinit var - 나중에 초기화
 * - mockk<Type>() - Mock 생성
 * - every { } returns - Stub 정의
 */
@DisplayName("PostServiceKt 단위 테스트")
class PostServiceKtTest {

    private lateinit var postServiceKt: PostServiceKt
    private lateinit var postRepositoryKt: PostRepositoryKt

    @BeforeEach
    fun setUp() {
        // MockK로 Repository Mock 생성
        postRepositoryKt = mockk<PostRepositoryKt>()
        postServiceKt = PostServiceKt(postRepositoryKt)
    }

    @AfterEach
    fun tearDown() {
        // Mock 정리
        clearAllMocks()
    }

    @Test
    @DisplayName("게시글 목록을 조회할 수 있다")
    fun getPosts() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val posts = listOf(
            PostKt(id = 1L, title = "제목1", content = "내용1", author = "작성자1"),
            PostKt(id = 2L, title = "제목2", content = "내용2", author = "작성자2")
        )
        val page = PageImpl(posts, pageable, posts.size.toLong())

        // Mock 설정: findAll이 호출되면 page를 반환
        every { postRepositoryKt.findAll(pageable) } returns page

        // When
        val result = postServiceKt.getPosts(pageable)

        // Then
        assertThat(result.posts).hasSize(2)
        assertThat(result.totalElements).isEqualTo(2)
        assertThat(result.currentPage).isEqualTo(0)
        assertThat(result.posts[0].title).isEqualTo("제목1")

        // Verify: findAll이 정확히 1번 호출되었는지 검증
        verify(exactly = 1) { postRepositoryKt.findAll(pageable) }
    }

    @Test
    @DisplayName("ID로 게시글 상세를 조회할 수 있다")
    fun getPost() {
        // Given
        val postId = 1L
        val post = PostKt(
            id = postId,
            title = "테스트 게시글",
            content = "테스트 내용",
            author = "작성자"
        )

        // Mock 설정
        every { postRepositoryKt.findByIdWithComments(postId) } returns post

        // When
        val result = postServiceKt.getPost(postId)

        // Then
        assertThat(result.id).isEqualTo(postId)
        assertThat(result.title).isEqualTo("테스트 게시글")
        assertThat(result.content).isEqualTo("테스트 내용")

        verify(exactly = 1) { postRepositoryKt.findByIdWithComments(postId) }
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외가 발생한다")
    fun getPost_NotFound() {
        // Given
        val postId = 999L

        // Mock 설정: null 반환
        every { postRepositoryKt.findByIdWithComments(postId) } returns null

        // When & Then
        assertThatThrownBy { postServiceKt.getPost(postId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("게시글을 찾을 수 없습니다")

        verify(exactly = 1) { postRepositoryKt.findByIdWithComments(postId) }
    }

    @Test
    @DisplayName("키워드로 게시글을 검색할 수 있다")
    fun searchPost() {
        // Given
        val keyword = "테스트"
        val pageable = PageRequest.of(0, 10)
        val posts = listOf(
            PostKt(id = 1L, title = "테스트 제목", content = "내용", author = "작성자")
        )
        val page = PageImpl(posts, pageable, posts.size.toLong())

        every { postRepositoryKt.searchByKeyword(keyword, pageable) } returns page

        // When
        val result = postServiceKt.searchPost(keyword, pageable)

        // Then
        assertThat(result.posts).hasSize(1)
        assertThat(result.posts[0].title).contains("테스트")

        verify(exactly = 1) { postRepositoryKt.searchByKeyword(keyword, pageable) }
    }

    @Test
    @DisplayName("게시글을 생성할 수 있다")
    fun createPost() {
        // Given
        val request = PostDtoKt.CreatePostRequest(
            title = "새 게시글",
            content = "새 내용",
            author = "새 작성자"
        )

        val savedPost = PostKt(
            id = 1L,
            title = request.title,
            content = request.content,
            author = request.author
        )

        // slot을 사용하여 save에 전달된 인자를 캡처
        val postSlot = slot<PostKt>()
        every { postRepositoryKt.save(capture(postSlot)) } returns savedPost

        // When
        val result = postServiceKt.createPost(request)

        // Then
        assertThat(result.id).isEqualTo(1L)
        assertThat(result.title).isEqualTo("새 게시글")
        assertThat(result.content).isEqualTo("새 내용")

        // Captured 값 검증
        assertThat(postSlot.captured.title).isEqualTo("새 게시글")

        verify(exactly = 1) { postRepositoryKt.save(any()) }
    }

    @Test
    @DisplayName("게시글을 수정할 수 있다")
    fun updatePost() {
        // Given
        val postId = 1L
        val existingPost = PostKt(
            id = postId,
            title = "기존 제목",
            content = "기존 내용",
            author = "작성자"
        )

        val request = PostDtoKt.UpdatePostRequest(
            title = "수정된 제목",
            content = "수정된 내용"
        )

        // MockK의 mockkStatic을 사용하여 확장 함수 Mock
        mockkStatic("org.springframework.data.repository.CrudRepositoryExtensionsKt")
        every { postRepositoryKt.findByIdOrNull(postId) } returns existingPost

        // When
        val result = postServiceKt.updatePost(postId, request)

        // Then
        assertThat(result.title).isEqualTo("수정된 제목")
        assertThat(result.content).isEqualTo("수정된 내용")
        // 엔티티가 실제로 업데이트되었는지 확인
        assertThat(existingPost.title).isEqualTo("수정된 제목")
        assertThat(existingPost.content).isEqualTo("수정된 내용")

        verify(exactly = 1) { postRepositoryKt.findByIdOrNull(postId) }
        // Dirty Checking이므로 save는 호출되지 않음
        verify(exactly = 0) { postRepositoryKt.save(any()) }

        unmockkStatic("org.springframework.data.repository.CrudRepositoryExtensionsKt")
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 예외가 발생한다")
    fun updatePost_NotFound() {
        // Given
        val postId = 999L
        val request = PostDtoKt.UpdatePostRequest(
            title = "수정된 제목",
            content = "수정된 내용"
        )

        mockkStatic("org.springframework.data.repository.CrudRepositoryExtensionsKt")
        every { postRepositoryKt.findByIdOrNull(postId) } returns null

        // When & Then
        assertThatThrownBy { postServiceKt.updatePost(postId, request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("게시글을 찾을 수 없습니다")

        verify(exactly = 1) { postRepositoryKt.findByIdOrNull(postId) }

        unmockkStatic("org.springframework.data.repository.CrudRepositoryExtensionsKt")
    }

    @Test
    @DisplayName("게시글을 삭제할 수 있다")
    fun deletePost() {
        // Given
        val postId = 1L

        every { postRepositoryKt.existsById(postId) } returns true
        every { postRepositoryKt.deleteById(postId) } just Runs

        // When
        postServiceKt.deletePost(postId)

        // Then
        verify(exactly = 1) { postRepositoryKt.existsById(postId) }
        verify(exactly = 1) { postRepositoryKt.deleteById(postId) }
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 예외가 발생한다")
    fun deletePost_NotFound() {
        // Given
        val postId = 999L

        every { postRepositoryKt.existsById(postId) } returns false

        // When & Then
        assertThatThrownBy { postServiceKt.deletePost(postId) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("게시글을 찾을 수 없습니다")

        verify(exactly = 1) { postRepositoryKt.existsById(postId) }
        verify(exactly = 0) { postRepositoryKt.deleteById(any()) }
    }
}
