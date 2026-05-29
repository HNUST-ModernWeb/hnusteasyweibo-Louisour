// service/UserService.java
package com.weibo.service;

import com.weibo.model.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {
    // 内存用户存储，key: 用户名, value: User对象
    private final Map<String, User> userStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void initUsers() {
        // 预设两个测试用户（密码明文，实际应用应加密）
        userStore.put("admin", new User("admin", "123456"));
        userStore.put("test", new User("test", "123456"));
        // 可根据需要添加更多用户
    }

    /**
     * 验证登录
     * @param username 用户名
     * @param password 密码
     * @return 验证成功返回User对象，失败返回null
     */
    public User authenticate(String username, String password) {
        User user = userStore.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * 检查用户是否存在
     */
    public boolean exists(String username) {
        return userStore.containsKey(username);
    }
}