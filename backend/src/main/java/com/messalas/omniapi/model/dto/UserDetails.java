package com.messalas.omniapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetails {

    public UserDetails(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    private Long id;
    private String username;
    private String password;
    private String role;

}
