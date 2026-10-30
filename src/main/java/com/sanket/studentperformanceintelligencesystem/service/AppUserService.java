package com.sanket.studentperformanceintelligencesystem.service;

import com.sanket.studentperformanceintelligencesystem.entity.AppUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AppUserService {
    AppUser saveUser(AppUser appUser);

    List<AppUser> getAllUsers();

    Page<AppUser> getAllUsers(Pageable pageable);

    AppUser getUserById(Long id);
    AppUser getUserByUsername(String username);
    AppUser getUserByEmail(String email);

    AppUser updateUser(Long id, AppUser appUser);

    void deleteUser(Long id);
}
