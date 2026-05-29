// model/Post.java
package com.weibo.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Post {
    private Long id;
    private String author;
    private String content;
    private String imageUrl;
    private LocalDateTime createTime;
    private List<Comment> comments;
    private Set<String> likedSessions;

    public Post(Long id, String author, String content, String imageUrl) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createTime = LocalDateTime.now();
        this.comments = new CopyOnWriteArrayList<>();
        this.likedSessions = ConcurrentHashMap.newKeySet();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public Set<String> getLikedSessions() { return likedSessions; }
    public void setLikedSessions(Set<String> likedSessions) { this.likedSessions = likedSessions; }

    public int getLikeCount() {
        return likedSessions.size();
    }
}