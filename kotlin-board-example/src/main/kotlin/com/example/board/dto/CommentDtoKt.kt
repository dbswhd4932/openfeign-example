package com.example.board.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class CommentDtoKt {

    data class CreateCommentRequest(
        @field: NotBlank(message = "내용은 필수입니다") val content: String,
        @field: NotBlank(message = "작성자는 필수입니다")
        @field: Size(max = 50, message = "작성자는 50자 이하여야 합니다")
        val author: String
    ) {

    }
}