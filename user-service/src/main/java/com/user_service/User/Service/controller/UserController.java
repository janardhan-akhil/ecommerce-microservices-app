package com.user_service.User.Service.controller;


import com.user_service.User.Service.dto.request.PageResponse;
import com.user_service.User.Service.dto.request.UserDto;
import com.user_service.User.Service.service.UserService;
import com.user_service.User.Service.utility.AppConstants;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")  // Only users with ADMIN role can create new users
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto){
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id.toString() == authentication.principal)")  // Admins can access any user, regular users can only access their own data
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }



    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")  // Only users with ADMIN role can access the list of all users
    public ResponseEntity<PageResponse> getUsers(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        System.out.println("Controller method called");

        return ResponseEntity.ok(userService.getAllUsers(pageNo, pageSize, sortBy, sortDir));


    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id.toString() == authentication.principal)")  // Admins can update any user, regular users can only update their own data
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto,@PathVariable Long id){
        return ResponseEntity.ok(userService.updateUser(userDto, id));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id.toString() == authentication.principal)")  // Admins can delete any user, regular users can only delete their own account
    public String deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return "User with id "+id+" deleted successfully";
    }
}
