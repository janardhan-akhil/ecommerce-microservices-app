package com.user_service.User.Service.utility;

import lombok.*;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ApiResponse {
    public String message;
    public boolean success;
    public HttpStatus status;
}
