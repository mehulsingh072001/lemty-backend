package com.lemty.server.service;

import com.lemty.server.domain.AppUser;
import com.lemty.server.domain.Role;

import java.util.List;
import java.util.Map;

public interface UserService {
    AppUser saveUser(AppUser user);
    Role saveRole(Role role);
    void addRoleToUser(String username, String roleName);
    AppUser getUser(String username);
    void updateUser(AppUser user, String id);
    void deleteUser(String id);
    List<AppUser>getUsers();
    Map<String, String> userById(String id);
}
