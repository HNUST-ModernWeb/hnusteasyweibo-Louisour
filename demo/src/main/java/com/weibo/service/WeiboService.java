// service/WeiboService.java
package com.weibo.service;

import com.weibo.model.Comment;
import com.weibo.model.Post;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class WeiboService {

    private final Map<Long, Post> posts = new ConcurrentHashMap<>();
    private final AtomicLong postIdGenerator = new AtomicLong(1);
    private final AtomicLong commentIdGenerator = new AtomicLong(1);
    private String uploadDir;

    public WeiboService(org.springframework.core.env.Environment env) {
        this.uploadDir = env.getProperty("file.upload-dir", "./uploads");
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @PostConstruct
    public void initDemoData() {
        Post demoPost = new Post(postIdGenerator.getAndIncrement(), "微博小助手",
                "欢迎来到简易微博！你可以发布动态，支持图片上传，也可以评论和点赞。", null);
        posts.put(demoPost.getId(), demoPost);

        // 添加一条评论
        Comment demoComment = new Comment(commentIdGenerator.getAndIncrement(), "访客", "界面很简洁，不错！");
        demoPost.getComments().add(demoComment);  // ✅ 直接操作 List<Comment>
    }

    public List<Post> getAllPosts() {
        return posts.values().stream()
                .sorted((p1, p2) -> p2.getCreateTime().compareTo(p1.getCreateTime()))
                .collect(Collectors.toList());
    }

    public Post getPostById(Long id) {
        return posts.get(id);
    }

    public Post createPost(String author, String content, MultipartFile image) throws IOException {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            String originalFilename = image.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + suffix;
            File dest = new File(uploadDir, fileName);
            image.transferTo(dest);
            imageUrl = "/uploads/" + fileName;
        }
        Long id = postIdGenerator.getAndIncrement();
        Post post = new Post(id, author, content, imageUrl);
        posts.put(id, post);
        return post;
    }

    public Comment addComment(Long postId, String author, String content) {
        Post post = posts.get(postId);
        if (post == null) {
            return null;
        }
        Long commentId = commentIdGenerator.getAndIncrement();
        Comment comment = new Comment(commentId, author, content);
        post.getComments().add(comment);   // ✅ 直接 add
        return comment;
    }

    public synchronized LikeResult likePost(Long postId, String sessionId) {
        Post post = posts.get(postId);
        if (post == null) {
            return null;
        }
        if (post.getLikedSessions().contains(sessionId)) {
            return new LikeResult(false, post.getLikeCount());
        }
        post.getLikedSessions().add(sessionId);
        return new LikeResult(true, post.getLikeCount());
    }

    public static class LikeResult {
        private final boolean success;
        private final int likeCount;

        public LikeResult(boolean success, int likeCount) {
            this.success = success;
            this.likeCount = likeCount;
        }
        public boolean isSuccess() { return success; }
        public int getLikeCount() { return likeCount; }
    }
}