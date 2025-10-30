package com.example.board.service

import com.example.board.dto.PostDtoKt
import com.example.board.repository.PostRepositoryKt
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PostServiceKt(
    private val postRepositoryKt: PostRepositoryKt
) {
    fun getPosts(pageable: Pageable): PostDtoKt.PostListResponse {
        val page = postRepositoryKt.findAll(pageable)

        return PostDtoKt.PostListResponse(
            posts = page.content.map {
                PostDtoKt.PostResponse.from(it) // it -> 현재요소
            },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            size = page.size
        )

    }
}