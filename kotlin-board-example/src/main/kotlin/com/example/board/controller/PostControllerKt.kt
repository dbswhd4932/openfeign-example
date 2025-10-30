package com.example.board.controller

import com.example.board.dto.PostDtoKt
import com.example.board.service.PostServiceKt
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/posts/kt")
class PostControllerKt(

    private val postServiceKt: PostServiceKt

) {
    @GetMapping
    fun getPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") direction: String,
    ): ResponseEntity<PostDtoKt.PostListResponse> {

        val sort = if (direction.equals("DESC", ignoreCase = true)) // true -> 대소문자 무시
            Sort.by(sortBy).descending()
        else Sort.by(sortBy).ascending()

        val pageable = PageRequest.of(page, size, sort)
        val response = postServiceKt.getPosts(pageable)

        return ResponseEntity.ok(response)

    }


}