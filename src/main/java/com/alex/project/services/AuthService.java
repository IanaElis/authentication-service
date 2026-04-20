package com.alex.project.services;

import com.alex.project.client.UserServiceClient;
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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    JwtService jwtService;

    @Inject
    @RestClient
    UserServiceClient userServiceClient;


    public String login(LoginDto loginDto) {
        User user =
                userRepository.findByUsername(loginDto.getUsername())
                        .orElseThrow(() -> new UserNotFoundException("Error"));
        if(!BcryptUtil.matches(loginDto.getPassword(), user.getPassword())) {
            throw new SecurityException("Error");
        }

        return jwtService.jwtGenerator(user.getUsername(), user.getRole(), user.getId());
    }

    @Transactional
    public String registration(RegistrationDto registrationDto) {
        User user = userCreation(registrationDto, Role.USER);

        CreateProfileDto createProfileDto = new CreateProfileDto(user.getUsername(), user.getId());

        Response response = userServiceClient.createProfile(createProfileDto);

        if (response.getStatus() >= 300) {
            throw new RuntimeException("Profile creation failed");
        }

        return jwtService.jwtGenerator(user.getUsername(), Role.USER, user.getId());
    }

    @Transactional
    public void registrationAdmin(RegistrationDto registrationDto){
        User user = userCreation(registrationDto, registrationDto.getRole());;

        CreateProfileDto createProfileDto = new CreateProfileDto(user.getUsername(), user.getId());

        Response response = userServiceClient.createProfile(createProfileDto);

        if (response.getStatus() >= 300) {
            throw new RuntimeException("Profile creation failed");
        }
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
