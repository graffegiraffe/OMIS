package com.codegen.controller;

import com.codegen.dto.ProjectDTO;
import com.codegen.model.GeneratedCode;
import com.codegen.model.Project;
import com.codegen.model.Requirement;
import com.codegen.model.User;
import com.codegen.repository.ProjectRepository;
import com.codegen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    //получить все проекты пользователя
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<ProjectDTO>> getUserProjects(@RequestHeader("User-Id") Long userId) {
        List<ProjectDTO> projects = projectRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projects);
    }

    //создать проект
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(
            @RequestHeader("User-Id") Long userId,
            @RequestBody ProjectDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setLanguage(dto.getLanguage());
        project.setFramework(dto.getFramework());
        project.setStatus(dto.getStatus());
        project.setUser(user);

        Project saved = projectRepository.save(project);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    //удалить проект
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private ProjectDTO mapToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setStatus(project.getStatus());
        dto.setLanguage(project.getLanguage());
        dto.setFramework(project.getFramework());
        dto.setUpdatedAt(project.getUpdatedAt());

        int files = 0;
        int lines = 0;
        if (project.getRequirements() != null) {
            for (Requirement req : project.getRequirements()) {
                if (req.getGeneratedCodes() != null) {
                    files += req.getGeneratedCodes().size();
                    for (GeneratedCode code : req.getGeneratedCodes()) {
                        if (code.getSourceCode() != null) {
                            lines += code.getSourceCode().split("\r\n|\r|\n").length;
                        }
                    }
                }
            }
        }
        dto.setFilesCount(files);
        dto.setLinesOfCode(lines);

        return dto;
    }
}
