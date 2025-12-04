package com.alex.project.services;

import com.alex.project.dtos.LoginDto;
import com.alex.project.entiies.Role;
import com.alex.project.entiies.User;
import com.alex.project.repositories.UserRepository;
import com.alex.project.utils.JwtGenerator;
import io.quarkus.elytron.security.common.BcryptUtil;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    JwtGenerator tokenGenerator;

    @InjectMocks
    AuthService authService;

    public AuthServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void successLogin() {
        User user = new User(1, "sashaporohnya76@gmail.com", "password123", Role.USER);

        when(userRepository.findByUsername("sashaporohnya76@gmail.com")).thenReturn(Optional.of(user));
        when(tokenGenerator.jwtGenerator(user.getUsername(), Role.USER)).thenReturn("token");

        try (MockedStatic<BcryptUtil> mocked = mockStatic(BcryptUtil.class)) {
            mocked.when(() -> BcryptUtil.matches("password123", user.getPassword())).thenReturn(true);

            LoginDto loginDto = new LoginDto("sashaporohnya76@gmail.com", "password123");

            String result = authService.login(loginDto);

            assertEquals("token", result);

            verify(userRepository).findByUsername("sashaporohnya76@gmail.com");
            verify(tokenGenerator).jwtGenerator(user.getUsername(), Role.USER);
        }
    }
}
