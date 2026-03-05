package com.alex.project.services;

//import com.alex.project.controllers.UserServiceClient;
import com.alex.project.dtos.CreateProfileDto;
import com.alex.project.dtos.LoginDto;
import com.alex.project.dtos.RegistrationDto;
import com.alex.project.entiies.Role;
import com.alex.project.entiies.User;
import com.alex.project.exceptions.UserAlreadyExist;
import com.alex.project.exceptions.UserNotFoundException;
import com.alex.project.repositories.UserRepository;
import com.alex.project.utils.JwtService;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.transaction.Status;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    JwtService tokenGenerator;
//
//    @Mock
//    UserServiceClient userServiceClient;

    @InjectMocks
    AuthService authService;

    public AuthServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class AuthServiceLoginTests{
        @Test
        void successLogin() {
            User user = new User(1L, "sashaporohnya76@gmail.com", "password123", Role.USER);

            when(userRepository.findByUsername("sashaporohnya76@gmail.com")).thenReturn(Optional.of(user));
            when(tokenGenerator.jwtGenerator(user.getUsername(), Role.USER, user.getId())).thenReturn("token");

            try (MockedStatic<BcryptUtil> mocked = mockStatic(BcryptUtil.class)) {
                mocked.when(() -> BcryptUtil.matches("password123", user.getPassword())).thenReturn(true);

                LoginDto loginDto = new LoginDto("sashaporohnya76@gmail.com", "password123");

                String result = authService.login(loginDto);

                assertEquals("token", result);

                verify(userRepository).findByUsername("sashaporohnya76@gmail.com");
                verify(tokenGenerator).jwtGenerator(user.getUsername(), Role.USER, user.getId());
            }
        }

        @Test
        void invalidPassword() {
            User user = new User(1L, "sashaporohnya76@gmail.com", "password123", Role.USER);

            when(userRepository.findByUsername("sashaporohnya76@gmail.com")).thenReturn(Optional.of(user));

            try (MockedStatic<BcryptUtil> mocked = mockStatic(BcryptUtil.class)) {
                mocked.when(() -> BcryptUtil.matches("password435", user.getPassword())).thenReturn(false);

                LoginDto loginDto = new LoginDto("sashaporohnya76@gmail.com", "password123");

                assertThrows(SecurityException.class, () -> authService.login(loginDto));
            }
        }

        @Test
        void emailDoesNotExist() {
            when(userRepository.findByUsername("sashaporohnya76@gmail.com")).thenReturn(Optional.empty());

            LoginDto loginDto = new LoginDto("sashaporohnya76@gmail.com", "password123");

            assertThrows(UserNotFoundException.class, () -> authService.login(loginDto));
        }
    }

    @Nested
    class AuthServiceRegistrationTests{
        @Test
        void successRegistration(){
            RegistrationDto registrationDto = new RegistrationDto("sashaporohnya76@gmail.com", "password123");
            CreateProfileDto createProfileDto = new CreateProfileDto("sashaporohnya76@gmail.com", 1L);
            Response response = Response.ok().build();

            when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.empty());

            try (MockedStatic<BcryptUtil> mocked = mockStatic(BcryptUtil.class)) {
                mocked.when(() -> BcryptUtil.bcryptHash(registrationDto.getPassword())).thenReturn("hash");
//                when(userServiceClient.createProfile(any(CreateProfileDto.class))).thenReturn(response);

                String result = authService.registration(registrationDto);

                assertEquals("token", result);

                ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

                when(tokenGenerator.jwtGenerator(registrationDto.getUsername(),
                        Role.USER, captor.capture().getId())).thenReturn("token");

                verify(userRepository).persistAndFlush(any());

                verify(userRepository).findByUsername(registrationDto.getUsername());
                mocked.verify(() -> BcryptUtil.bcryptHash(registrationDto.getPassword()));
            }
        }

        @Test
        void emailAlreadyExists(){
            User user = new User(1L, "sashaporohnya76@gmail.com", "password123", Role.USER);
            RegistrationDto registrationDto = new RegistrationDto("sashaporohnya76@gmail.com", "password123");

            when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(Optional.of(user));

            assertThrows(UserAlreadyExist.class, () -> authService.registration(registrationDto));
        }
    }
}
