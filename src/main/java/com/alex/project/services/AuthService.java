package com.alex.project.services;

import com.alex.project.dtos.LoginDto;
import com.alex.project.dtos.RegistrationDto;
import com.alex.project.entiies.Role;
import com.alex.project.entiies.User;
import com.alex.project.exceptions.UserAlreadyExist;
import com.alex.project.exceptions.UserNotFoundException;
import com.alex.project.repositories.UserRepository;
import com.alex.project.utils.JwtGenerator;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;
    @Inject
    JwtGenerator jwtService;

    public String login(LoginDto loginDto) {
        User user =
                userRepository.findByUsername(loginDto.getUsername())
                        .orElseThrow(() -> new UserNotFoundException("Error"));
        if(!BcryptUtil.matches(loginDto.getPassword(), user.getPassword())) {
            throw new SecurityException("Error");
        }

        return jwtService.jwtGenerator(user.getUsername(), user.getPassword());
    }

    @Transactional
    public String registration(RegistrationDto registrationDto) {
        if(userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new UserAlreadyExist("Username already exist");
        }
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        String hashedPassword = BcryptUtil.bcryptHash(registrationDto.getPassword());
        user.setPassword(hashedPassword);
        user.setRole(Role.USER);

        userRepository.persistAndFlush(user);

        return jwtService.jwtGenerator(user.getUsername(), hashedPassword);
    }
}
