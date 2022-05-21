package com.lemty.server.service;

import com.lemty.server.LemtyApplication;
import com.lemty.server.domain.AppUser;
import com.lemty.server.domain.DeliveribilitySettings;
import com.lemty.server.domain.Role;
import com.lemty.server.repo.RoleRepo;
import com.lemty.server.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    // public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final DeliveribilitySettingsService deliveribilitySettingsService;
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);


    public UserServiceImpl(UserRepo userRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder, DeliveribilitySettingsService deliveribilitySettingsService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.deliveribilitySettingsService = deliveribilitySettingsService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepo.findByUsername(username);
        if(user == null){
            logger.error("AppUser not found in database");
            throw new UsernameNotFoundException("AppUser not found in the database");
        }
        else{
            logger.info("AppUser found in the database: {}", username);
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(user.getRole()));
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
        }
    }

    @Override
    public AppUser saveUser(AppUser user) {
        AppUser existingUser = userRepo.findByUsername(user.getUsername());
        if(existingUser != null){
            logger.info("User exists");
        }
        else{
            logger.info("Saving new user {} to the database", user.getUsername());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles("ROLE_USER");
            AppUser savedUser = userRepo.save(user);

            DeliveribilitySettings settings = new DeliveribilitySettings();
            settings.setSeconds(240);
            settings.setEmailInterval("custom");

            deliveribilitySettingsService.createDeliveribilitySettings(settings, savedUser.getId());
            return savedUser;

        }
        return existingUser;
    }

    @Override
    public Role saveRole(Role role) {
        logger.info("Saving new role {} to the database", role.getName());
        return roleRepo.save(role);
    }

    @Override
    public void addRoleToUser(String username, String roleName) {
    }

    @Override
    public AppUser getUser(String username) {
        logger.info("Fetching user {}", username);
        return userRepo.findByUsername(username);
    }

    @Override
    public List<AppUser> getUsers() {
        logger.info("Fetching all users");
        return userRepo.findAll();
    }

    public void updateUser(AppUser newUser, String id){
        if(userRepo.findById(id).isPresent()){
            userRepo.findById(id)
                .map(user -> {
                    user.setId(id);
                    if(newUser.getFirstName() == null){
                        user.setFirstName(user.getFirstName());
                    }
                    else{
                        user.setFirstName(newUser.getFirstName());
                    }
                    if(newUser.getLastName() == null){
                        user.setLastName(user.getLastName());
                    }
                    else{
                        user.setLastName(newUser.getLastName());
                    }
                    if(newUser.getUsername() == null){
                        user.setUsername(user.getUsername());
                    }
                    else{
                        user.setUsername(newUser.getUsername());
                    }
                    if(newUser.getPassword() == null){
                        user.setPassword(user.getPassword());
                    }
                    else{
                        user.setPassword(passwordEncoder.encode(newUser.getPassword()));
                    }
                    if(newUser.getCompany_name() == null){
                        user.setCompany_name(user.getCompany_name());
                    }
                    else{
                        user.setCompany_name(newUser.getCompany_name());
                    }
                    if(newUser.getPhone_number() == null){
                        user.setPhone_number(user.getPhone_number());
                    }
                    else{
                        user.setPhone_number(newUser.getPhone_number());
                    }
                    if(newUser.getWork_email() == null){
                        user.setWork_email(user.getWork_email());
                    }
                    else{
                        user.setWork_email(newUser.getWork_email());
                    }
                    return userRepo.saveAndFlush(user);
                })
            .orElseGet(() -> {
                newUser.setId(id);
                return userRepo.saveAndFlush(newUser);
            });
        }
        else{
            logger.error("User Not Found");
        }
    }

    public void deleteUser(String userId){
        userRepo.deleteById(userId);
    }
    
    public Map<String, String> userById(String id) {
        Map<String, String> response = new HashMap<>();
        if(userRepo.findById(id).isPresent()){
            AppUser user = userRepo.findById(id).get();
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
        }
        return response;
    }

}
