package com.codegen.config;

import com.codegen.model.CodeTemplate;
import com.codegen.model.TemplateType;
import com.codegen.model.User;
import com.codegen.model.UserRole;
import com.codegen.repository.CodeTemplateRepository;
import com.codegen.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, CodeTemplateRepository templateRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User user = new User();
                user.setUsername("developer");
                user.setEmail("dev@example.com");
                user.setPassword("password");
                user.setRole(UserRole.DEVELOPER);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
                System.out.println(">>> Test User Created with ID: " + user.getId());
            }

            if (templateRepository.count() == 0) {
                // TypeScript
                CodeTemplate reactTemplate = new CodeTemplate();
                reactTemplate.setName("React Component Template");
                reactTemplate.setDescription("Базовый шаблон для React компонента");
                reactTemplate.setLanguage("typescript");
                reactTemplate.setFramework("react");
                reactTemplate.setType(TemplateType.ALGORITHM);
                reactTemplate.setTemplateContent(
                        "import React from 'react';\n" +
                                "const {componentName} = ({props}) => {\n" +
                                "  return <div>{content}</div>;\n" +
                                "};\n" +
                                "export default {componentName};"
                );
                reactTemplate.setVariables("{\"componentName\": \"MyComponent\", \"props\": \"\", \"content\": \"\"}");
                reactTemplate.setUsageCount(0);
                reactTemplate.setCreatedAt(LocalDateTime.now());
                templateRepository.save(reactTemplate);

                // Spring
                CodeTemplate javaTemplate = new CodeTemplate();
                javaTemplate.setName("Spring Controller Template");
                javaTemplate.setDescription("Базовый шаблон для Spring контроллера");
                javaTemplate.setLanguage("java");
                javaTemplate.setFramework("spring");
                javaTemplate.setType(TemplateType.CONTROLLER);
                javaTemplate.setTemplateContent(
                        "package com.example;\n" +
                                "import org.springframework.web.bind.annotation.*;\n" +
                                "@RestController\n" +
                                "@RequestMapping(\"/api\")\n" +
                                "public class {className} {\n" +
                                "    @GetMapping(\"/{endpoint}\")\n" +
                                "    public String {methodName}() {\n" +
                                "        return \"{response}\";\n" +
                                "    }\n" +
                                "}"
                );
                javaTemplate.setVariables("{\"className\": \"MyController\", \"endpoint\": \"test\", \"methodName\": \"getData\", \"response\": \"Hello\"}");
                javaTemplate.setUsageCount(0);
                javaTemplate.setCreatedAt(LocalDateTime.now());
                templateRepository.save(javaTemplate);

                System.out.println(">>> Templates Initialized");
            }
        };
    }
}