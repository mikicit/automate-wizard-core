package dev.mikita.automatewizard.controller;

import dev.mikita.automatewizard.dto.response.UserResponse;
import dev.mikita.automatewizard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping(path = "/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") UUID id){
        return ResponseEntity.ok(new ModelMapper().map(userService.getUser(id), UserResponse.class));
    }
}
