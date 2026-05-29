// model/Comment.java
package com.weibo.model;

import java.time.LocalDateTime;

public class Comment {
    private Long id;
    private String author;
    private String content;
    private LocalDateTime createTime;

    public Comment(Long id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.createTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}