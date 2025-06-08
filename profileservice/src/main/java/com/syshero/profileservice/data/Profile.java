package com.syshero.profileservice.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table
public class Profile {
    @Id
    private long id;
    private String email;
    private String name;
    private String status;
    private String role;
}
