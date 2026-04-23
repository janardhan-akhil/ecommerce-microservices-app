package com.user_service.User.Service.service.impl;

import com.user_service.User.Service.dto.request.PageResponse;
import com.user_service.User.Service.dto.request.UserDto;
import com.user_service.User.Service.entity.User;
import com.user_service.User.Service.exception.ResourceNotFoundException;
import com.user_service.User.Service.repository.UserRepository;
import com.user_service.User.Service.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    ModelMapper modelMapper;
    UserRepository userRepository;
    public UserServiceImpl(ModelMapper modelMapper, UserRepository userRepository) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
    }


    @Override
    public UserDto createUser(UserDto userDto) {
       return  mapToDTO(userRepository.save(mapToEntity(userDto)));
    }

    @Override
    public UserDto getUserById(Long id) {
       return mapToDTO(userRepository.findById(id).orElseThrow(ResourceNotFoundException::new));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long id) {
        User user = userRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
            user.setName(userDto.getName());
            user.setEmail(userDto.getEmail());
            user.setPassword(userDto.getPassword());
            user.setRole(userDto.getRole());
            user.setCreated_at(userDto.getCreated_at());
           return  mapToDTO(userRepository.save(user));
    }

    @Override
    public PageResponse getAllUsers(int pageNo, int pageSize, String sortBy, String sortDir) {

        Logger.getLogger(UserServiceImpl.class.getName()).log(Level.INFO, "sortBy: " + sortBy);
        System.out.println("Hello from getAllUsers method");
        System.out.println("SORT BY = " + sortBy);
        System.out.println("SORT DIR = " + sortDir);
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(pageNo,pageSize, sort);
        Page<User> users = userRepository.findAll(pageable);
        List<User> content = users.getContent();
        List<UserDto> userDtos = content.stream().map(this::mapToDTO).collect(Collectors.toList());
        return PageResponse.builder()
                .content(userDtos)
                .pageNo(users.getNumber())
                .pageSize(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .last(users.isLast())
                .build();
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    public UserDto mapToDTO(User user) {
        return modelMapper.map(user, UserDto.class);
    }
    public User mapToEntity(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }


}
