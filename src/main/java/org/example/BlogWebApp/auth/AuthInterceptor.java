package org.example.BlogWebApp.auth;

import static jakarta.servlet.http.HttpServletResponse.*;

import jakarta.servlet.http.*;
import org.example.BlogWebApp.entities.User;
import org.example.BlogWebApp.mappers.UserMapper;
import org.example.SpringFramework.SpringBoot.interceptors.HandlerInterceptor;
import org.example.SpringFramework.SpringBoot.interceptors.HandlerMethod;
import org.example.SpringFramework.SpringContainer.annotations.beans.Autowired;
import org.example.SpringFramework.SpringContainer.annotations.beans.Component;

import java.util.Base64;

@Component
public class AuthInterceptor extends HandlerInterceptor {
    @Autowired
    UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String[] authPair = getAuthPair(request);
        if (authPair == null) {
            response.setStatus(SC_UNAUTHORIZED);
            return false;
        }

        String username = authPair[0];
        String password = authPair[1];
        User user = userMapper.getUser(username);

        boolean isAuthenticated = authenticate(user, password);
        if (!isAuthenticated) {
            response.setStatus(SC_UNAUTHORIZED);
            return false;
        }

        request.setAttribute("user", username);

        boolean isAuthorized = authorize(user, handler);
        if (!isAuthorized) {
            response.setStatus(SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    private boolean authorize(User user, Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Role roleAnn = (Role) handlerMethod.getMethodAnnotation(Role.class);
        if (roleAnn == null)
            return true;

        String role = roleAnn.value();
        return role.equals(user.role);
    }

    private boolean authenticate(User user, String password) {
        if (user == null)
            return false;

        String encryptedPassword = PasswordEncryptor.encryptPassword(password + user.salt);
        return encryptedPassword.equals(user.password);
    }

    private String[] getAuthPair(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null)
            return null;

        String encodedStr = authHeader.split(" ")[1];
        byte[] decodedBytes = Base64.getDecoder().decode(encodedStr);
        String authStr = new String(decodedBytes);
        return authStr.split(":");
    }
}
