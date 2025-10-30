package com.example.board.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "comments")
data class CommentsKt(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false, length = 50)
    var author: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    var post: PostKt? = null
) {
    fun update(content: String) {
        this.content = content
        this.updatedAt = LocalDateTime.now()
    }

}
