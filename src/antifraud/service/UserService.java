package antifraud.service;

import antifraud.model.Enum.UserAccess;
import antifraud.model.dto.*;
import antifraud.model.entity.Role;
import antifraud.model.entity.User;
import antifraud.request.SetAccessRequest;
import antifraud.request.SetRoleRequest;
import antifraud.response.UserAccessResponse;
import antifraud.response.UserDeleteResponse;
import antifraud.repository.UserDetailRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final static String ADMINISTRATOR = "ADMINISTRATOR";
    private final static String MERCHANT = "MERCHANT";
    private final static String SUPPORT = "SUPPORT";

    private final UserDetailRepository userDetailRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserService(UserDetailRepository userDetailRepository, PasswordEncoder encoder) {
        this.userDetailRepository = userDetailRepository;
        this.encoder = encoder;
    }


    public UserDto registerUser(User user) {
        String username = user.getUsername().toLowerCase();

        Optional<User> existingUser = userDetailRepository.findByUsername(username);

        if (existingUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The username has already been taken!");
        } else {
            user.setUsername(username);
            user.setPassword(encoder.encode(user.getPassword()));

            userDetailRepository.save(user);

            User savedUser = userDetailRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found after saving."));

            if (savedUser.getId() == 1) {
                savedUser.addRole(new Role("ADMINISTRATOR"));
                savedUser.setAccountNonLocked(true);
            } else {
                savedUser.addRole(new Role("MERCHANT"));
                savedUser.setAccountNonLocked(false);
            }

            savedUser = userDetailRepository.save(savedUser);

            return new UserDto(savedUser.getId(), savedUser.getName(),
                    savedUser.getUsername(), savedUser.getRoles().get(0).getName());
        }
    }

    public List<UserDto> findAll() {
        List<User> users = userDetailRepository.findAll();

        List<UserDto> userDtos = users.stream()
                .map(user -> new UserDto(user.getId(), user.getName(),
                        user.getUsername(), user.getRoles().get(0).getName()))
                .sorted(Comparator.comparing(UserDto::getId))
                .collect(Collectors.toList());

        return userDtos;
    }

    @Transactional
    public UserDeleteResponse deleteByUsername(String username) {
        if (!userDetailRepository.findByUsername(username.toLowerCase()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot found user with username: " + username);
        }
        userDetailRepository.deleteByUsername(username);

        return new UserDeleteResponse(username);
    }

    public UserDto setUserRole(SetRoleRequest setRoleRequest) {

        if (!userDetailRepository.findByUsername(setRoleRequest.getUsername().toLowerCase()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cannot found user with username: " + setRoleRequest.getUsername());
        }

        if (!setRoleRequest.getRole().equals(MERCHANT) && !setRoleRequest.getRole().equals(SUPPORT)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "You cannot assign the role ["
                                                + setRoleRequest.getRole()
                                                + "] to user");
        }

        User savedUser = userDetailRepository.findByUsername(setRoleRequest.getUsername().toLowerCase()).get();

        if (setRoleRequest.getRole().equals(savedUser.getRoles().get(0).getName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This role has already been assigned to the user: " + setRoleRequest.getRole());
        }

        List<Role> newRole = new ArrayList<>();
        newRole.add(new Role(setRoleRequest.getRole()));

        savedUser.setRoles(newRole);

        userDetailRepository.save(savedUser);

        return new UserDto(savedUser.getId(), savedUser.getName(),
                savedUser.getUsername(), savedUser.getRoles().get(0).getName());
    }


    public UserAccessResponse setUserAccess(SetAccessRequest setAccessRequest) {
        if (!userDetailRepository.findByUsername(setAccessRequest.getUsername().toLowerCase()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cannot found user with username: " + setAccessRequest.getUsername());
        }

        User savedUser = userDetailRepository.findByUsername(setAccessRequest.getUsername().toLowerCase()).get();

        if (savedUser.getRoles().get(0).getName().equals(ADMINISTRATOR)
                && setAccessRequest.getOperation() == UserAccess.LOCK) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot the lock the admin!");
        }

        if (setAccessRequest.getOperation()==UserAccess.LOCK) {
            // lock the user
            savedUser.setAccountNonLocked(false);
            userDetailRepository.save(savedUser);
        }

        if (setAccessRequest.getOperation()==UserAccess.UNLOCK) {
            // unlock the user
            savedUser.setAccountNonLocked(true);
            userDetailRepository.save(savedUser);
        }

        return new UserAccessResponse(setAccessRequest.getUsername(), setAccessRequest.getOperation());
    }
}
