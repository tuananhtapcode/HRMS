package com.project.hrms.service;

import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.models.User;

import java.util.List;

public interface IUserService {
    User createUser(UserDTO userDTO);

    String loginUser(String phoneNumber, String password);

    User getUserById(int id);

    List<User> getAllUser();

    User updateUser(long userId, UserDTO userDTO);

    void deleteUser(long userId);
}
