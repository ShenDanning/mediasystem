package com.xxb.mediasystem.interceptor;


import com.xxb.mediasystem.model.User;

/**
 * @description: 线程级共享user
 * @author: 0GGmr0
 * @create: 2019-12-01 21:38
 */
public class UserContext implements AutoCloseable {
    private static final ThreadLocal<User> current = new ThreadLocal<>();

    public UserContext(User user) {
        current.set(user);
    }

    public static User getCurrentUser() {
        return current.get();
    }

    @Override
    public void close() {
        current.remove();
    }
}
