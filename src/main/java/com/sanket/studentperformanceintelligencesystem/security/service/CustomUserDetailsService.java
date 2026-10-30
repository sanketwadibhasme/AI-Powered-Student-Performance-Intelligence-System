package com.sanket.studentperformanceintelligencesystem.security.service;

import com.sanket.studentperformanceintelligencesystem.entity.AppUser;
import com.sanket.studentperformanceintelligencesystem.repository.AppUserRepository;
import com.sanket.studentperformanceintelligencesystem.repository.FacultyRepository;
import com.sanket.studentperformanceintelligencesystem.security.user.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    private final FacultyRepository facultyRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository, FacultyRepository facultyRepository) {
        this.appUserRepository = appUserRepository;
        this.facultyRepository = facultyRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // A FACULTY account has no app_users column linking to faculties; the
        // faculty record is matched by its unique email. Resolved once here so
        // faculty self-service endpoints (/api/faculties/me/subjects) can use it.
        if ("FACULTY".equals(appUser.getRole().name()) && appUser.getFacultyId() == null) {
            facultyRepository.findByEmail(appUser.getEmail()).ifPresent(f -> appUser.setFacultyId(f.getFacultyId()));
        }

        return new CustomUserDetails(appUser);
    }
}
