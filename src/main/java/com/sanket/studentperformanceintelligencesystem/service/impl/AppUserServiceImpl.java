package com.sanket.studentperformanceintelligencesystem.service.impl;

import com.sanket.studentperformanceintelligencesystem.entity.AppUser;
import com.sanket.studentperformanceintelligencesystem.entity.Student;
import com.sanket.studentperformanceintelligencesystem.enums.Role;
import com.sanket.studentperformanceintelligencesystem.exception.ResourceNotFoundException;
import com.sanket.studentperformanceintelligencesystem.repository.AppUserRepository;
import com.sanket.studentperformanceintelligencesystem.repository.StudentRepository;
import com.sanket.studentperformanceintelligencesystem.service.AppUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserServiceImpl implements AppUserService{
    private final AppUserRepository appUserRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserServiceImpl(
            AppUserRepository appUserRepository,
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {

        this.appUserRepository = appUserRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public AppUser saveUser(AppUser appUser) {
        String encodedPassword = passwordEncoder.encode(appUser.getPassword());
        appUser.setPassword(encodedPassword);

        applyStudentLink(appUser);

        return appUserRepository.save(appUser);
    }

    @Override
    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    @Override
    public Page<AppUser> getAllUsers(Pageable pageable) {
        return appUserRepository.findAll(pageable);
    }

    @Override
    public AppUser getUserById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + id));
    }
    @Override
    public AppUser getUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username : " + username));
    }
    @Override
    public AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email : " + email));
    }

    @Override
    public AppUser updateUser(Long id, AppUser appUser) {
        AppUser existingUser = getUserById(id);

        existingUser.setUsername(appUser.getUsername());
        existingUser.setEmail(appUser.getEmail());

        // Re-encode only when a new password was actually supplied; a null/blank
        // password means "keep the current one" so the admin edit form can omit it.
        if (appUser.getPassword() != null && !appUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        }

        existingUser.setRole(appUser.getRole());
        existingUser.setEnabled(appUser.getEnabled() != null ? appUser.getEnabled() : existingUser.getEnabled());

        // A role moved away from STUDENT can no longer keep a student link.
        if (existingUser.getRole() != Role.STUDENT) {
            existingUser.setStudent(null);
        }

        applyStudentLink(appUser, existingUser);

        return appUserRepository.save(existingUser);
    }

    /**
     * Resolves the transient {@code studentId} from the incoming payload into
     * the actual {@link Student} entity. An explicit null clears the link,
     * which is only meaningful for STUDENT accounts. Keeps the existing link
     * when the payload omits the field (null on a fresh instance).
     */
    private void applyStudentLink(AppUser incoming) {
        applyStudentLink(incoming, incoming);
    }

    private void applyStudentLink(AppUser incoming, AppUser target) {
        // A fresh deserialized payload carries the id in the transient field;
        // target (existing entity) exposes the current link via getStudentId().
        if (incoming.getStudentId() == null && incoming.getStudent() == null) {
            return; // field omitted -> leave existing link untouched
        }
        if (incoming.getStudentId() == null) {
            target.setStudent(null);
            return;
        }
        // 0 or negative is the frontend's explicit "unlink" signal
        if (incoming.getStudentId() <= 0) {
            target.setStudent(null);
            return;
        }
        if (target.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Only STUDENT accounts can be linked to a student record.");
        }
        Student student = studentRepository.findById(incoming.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with ID " + incoming.getStudentId()));
        target.setStudent(student);
    }

    @Override
    public void deleteUser(Long id) {
        AppUser appUser = getUserById(id);
        appUserRepository.delete(appUser);
    }
}
