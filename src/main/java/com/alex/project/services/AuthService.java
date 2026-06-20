package com.alex.project.services;

import com.alex.project.clients.UserServiceClient;
import com.alex.project.dtos.user.CreateProfileDto;
import com.alex.project.dtos.LoginDto;
import com.alex.project.dtos.RegistrationDto;
import com.alex.project.entiies.Role;
import com.alex.project.entiies.User;
import com.alex.project.exceptions.UserAlreadyExist;
import com.alex.project.exceptions.UserNotFoundException;
import com.alex.project.repositories.UserRepository;
import com.alex.project.utils.JwtService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    JwtService jwtService;

    @Inject
    @RestClient
    UserServiceClient userServiceClient;

    @Transactional
    public User authenticate(LoginDto loginDto) {
        User user =
                userRepository.findByUsername(loginDto.getUsername())
                        .orElseThrow(() -> new UserNotFoundException("Error"));
        if(!BcryptUtil.matches(loginDto.getPassword(), user.getPassword())) {
            throw new SecurityException("Error");
        }
        return user;
    }

    @Transactional
    public User registerUser(RegistrationDto registrationDto, Role role) {
        User user = userCreation(registrationDto, role);

        CreateProfileDto createProfileDto = new CreateProfileDto(user.getUsername(), user.getId());

        Uni<Response> response = userServiceClient.createProfile(createProfileDto);

        if (response.await().atMost(Duration.ofSeconds(15L)).getStatus() >= 300) {
            throw new RuntimeException("Profile creation failed");
        }

        return user;
    }

    private User userCreation(RegistrationDto registrationDto, Role role) {
        if(userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new UserAlreadyExist("Username already exist");
        }
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        String hashedPassword = BcryptUtil.bcryptHash(registrationDto.getPassword());
        user.setPassword(hashedPassword);

        user.setRole(role);

        userRepository.persistAndFlush(user);

        return user;
    }
}
