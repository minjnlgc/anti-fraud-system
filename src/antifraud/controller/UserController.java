package antifraud.controller;

import antifraud.model.dto.*;
import antifraud.model.entity.User;
import antifraud.request.SetAccessRequest;
import antifraud.request.SetRoleRequest;
import antifraud.response.UserAccessResponse;
import antifraud.response.UserDeleteResponse;
import antifraud.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody User user) {
        return userService.registerUser(user);
    }

    @GetMapping("/list")
    public List<UserDto> getUserList() {
        return userService.findAll();
    }

    @DeleteMapping("/user/{username}")
    public UserDeleteResponse deleteUser(@PathVariable String username) {
        return userService.deleteByUsername(username);
    }

    @PutMapping("/role")
    public UserDto changeUserRole(@Valid @RequestBody SetRoleRequest setRoleRequest) {
        return userService.setUserRole(setRoleRequest);
    }

    @PutMapping("/access")
    public UserAccessResponse changeUserAccess(@Valid @RequestBody SetAccessRequest setAccessRequest) {
        return userService.setUserAccess(setAccessRequest);
    }
}
