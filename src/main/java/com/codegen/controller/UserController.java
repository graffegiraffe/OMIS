package com.codegen.controller;

import com.codegen.dto.UserDTO;
import com.codegen.model.User;
import com.codegen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return userRepository.findById(id)
                .map(user -> {
                    if(userDTO.getFullName() != null) user.setFullName(userDTO.getFullName());
                    if(userDTO.getCompany() != null) user.setCompany(userDTO.getCompany());
                    if(userDTO.getJobTitle() != null) user.setJobTitle(userDTO.getJobTitle());
                    if(userDTO.getExperienceYears() != null) user.setExperienceYears(userDTO.getExperienceYears());
                    if(userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) user.setEmail(userDTO.getEmail());

                    userRepository.save(user);
                    return ResponseEntity.ok(mapToDTO(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setFullName(user.getFullName() != null ? user.getFullName() : user.getUsername());
        dto.setCompany(user.getCompany() != null ? user.getCompany() : "Не указано");
        dto.setJobTitle(user.getJobTitle() != null ? user.getJobTitle() : "Разработчик");
        dto.setExperienceYears(user.getExperienceYears() != null ? user.getExperienceYears() : "Не указано");
        return dto;
    }
}
