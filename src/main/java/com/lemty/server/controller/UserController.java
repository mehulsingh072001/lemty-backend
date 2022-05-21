package com.lemty.server.controller;

import com.lemty.server.domain.AppUser;
import com.lemty.server.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getUsers() {
        return (ResponseEntity<List<AppUser>>) ResponseEntity.ok().body(userService.getUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> getUsersById(@PathVariable("userId") String userId) {
        return (ResponseEntity<Map<String, String>>) ResponseEntity.ok().body(userService.userById(userId));
    }

    @PostMapping("/register")
    public ResponseEntity<AppUser>saveUser(@RequestBody AppUser user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/register").toUriString());
        return ResponseEntity.created(uri).body(userService.saveUser(user));
    }

    @PutMapping("/users/update/{userId}")
    public ResponseEntity<AppUser>updateUser(@RequestBody AppUser newUser, @PathVariable("userId") String userId){
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/update").toUriString());
        userService.updateUser(newUser, userId);
        return ResponseEntity.created(uri).body(newUser);
    }

    @DeleteMapping(path = "/users/delete/{userId}")
    public void deleteCampaign(@PathVariable("userId") String userId){
        userService.deleteUser(userId);
    }
}

class RoleToUserForm {
    private String username;
    private String roleName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
