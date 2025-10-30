package com.example.board.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "post")
data class PostKt(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false, length = 50)
    var author: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "postKt", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: ArrayList<CommentsKt?> = ArrayList()
) {
    // 비즈니스 메서드
    fun addComment(commentKt: CommentsKt) {
        comments.add(commentKt)
        commentKt.post = this
    }

    fun removeComment(commentKt: CommentsKt) {
        comments.remove(commentKt)
        commentKt.post = null
    }

    fun update(title: String, content: String) {
        this.title = title
        this.content = content
        this.updatedAt = LocalDateTime.now()
    }
}
