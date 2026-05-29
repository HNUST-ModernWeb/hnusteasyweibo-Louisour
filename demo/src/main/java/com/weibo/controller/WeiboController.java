// controller/WeiboController.java
package com.weibo.controller;

import com.weibo.model.Comment;
import com.weibo.model.Post;
import com.weibo.model.User;
import com.weibo.service.WeiboService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WeiboController {

    @Autowired
    private WeiboService weiboService;

    // 首页，展示所有动态
    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        List<Post> posts = weiboService.getAllPosts();
        model.addAttribute("posts", posts);
        HttpSession session = request.getSession(false);
        if (session != null) {
            User loginUser = (User) session.getAttribute("loginUser");
            if (loginUser != null) {
                model.addAttribute("username", loginUser.getUsername());
            }
        }
        return "index";
    }

    // API: 获取所有动态JSON
    @GetMapping("/api/posts")
    @ResponseBody
    public List<Post> getPosts() {
        return weiboService.getAllPosts();
    }

    // 发布动态 - 从Session获取登录用户作为作者
    @PostMapping("/api/posts")
    @ResponseBody
    public ResponseEntity<?> createPost(@RequestParam("content") String content,
                                        @RequestParam(value = "image", required = false) MultipartFile image,
                                        HttpSession session) {
        // 获取登录用户（拦截器保证已登录）
        User loginUser = (User) session.getAttribute("loginUser");
        String author = loginUser.getUsername();
        
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "动态内容不能为空"));
        }
        try {
            Post post = weiboService.createPost(author.trim(), content.trim(), image);
            return ResponseEntity.ok(post);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "图片上传失败: " + e.getMessage()));
        }
    }

    // 添加评论 - 从Session获取登录用户作为评论者
    @PostMapping("/api/comments")
    @ResponseBody
    public ResponseEntity<?> addComment(@RequestParam("postId") Long postId,
                                        @RequestParam("content") String content,
                                        HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        String author = loginUser.getUsername();
        
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "评论内容不能为空"));
        }
        Comment comment = weiboService.addComment(postId, author.trim(), content.trim());
        if (comment == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("comment", comment);
        result.put("postId", postId);
        return ResponseEntity.ok(result);
    }

    // 点赞
    @PostMapping("/api/like")
    @ResponseBody
    public ResponseEntity<?> likePost(@RequestParam("postId") Long postId,
                                      HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        WeiboService.LikeResult result = weiboService.likePost(postId, sessionId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("success", result.isSuccess());
        map.put("likeCount", result.getLikeCount());
        map.put("postId", postId);
        return ResponseEntity.ok(map);
    }
}