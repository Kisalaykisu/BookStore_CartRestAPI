package com.example.bookstore_cartrestapi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
public class UserDataDTO {
    Long userId;
    String firstName;
    String lastName;
    String address;
    String emailAddress;
    LocalDate DOB;
    String password;
}
