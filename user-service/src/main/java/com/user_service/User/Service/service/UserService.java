package com.user_service.User.Service.service;

import com.user_service.User.Service.dto.request.PageResponse;
import com.user_service.User.Service.dto.request.UserDto;

public interface UserService {

    public UserDto createUser(UserDto userDto);
    public UserDto getUserById(Long id);
    public UserDto updateUser(UserDto userDto, Long id);
    public void deleteUser(Long id);
   PageResponse getAllUsers(int pageNo, int pageSize, String sortBy, String sortDir);

}
