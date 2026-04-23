package com.user_service.User.Service.service;


import com.user_service.User.Service.dto.request.PageResponse;
import com.user_service.User.Service.dto.request.UserDto;
import com.user_service.User.Service.entity.User;
import com.user_service.User.Service.repository.UserRepository;
import com.user_service.User.Service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@test.com");
        user.setPassword("123");
        user.setRole("USER");

        userDto = new UserDto();
        userDto.setName("John");
        userDto.setEmail("john@test.com");
        userDto.setPassword("123");
        userDto.setRole("USER");
    }

    @Test
    void testCreateUser() {
        when(modelMapper.map(userDto, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("John", result.getName());
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.updateUser(userDto, 1L);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testGetAllUsers() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        PageResponse response = userService.getAllUsers(0, 2, "name", "ASC");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}
