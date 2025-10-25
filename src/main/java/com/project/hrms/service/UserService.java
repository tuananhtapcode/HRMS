package com.project.hrms.service;

import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.PermissionDenyException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final LocalizationUtils localizationUtils;
    @Override
    public User createUser(UserDTO userDTO) {
        //register user
        String phoneNumber = userDTO.getPhoneNumber();
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("So dien thoai da duoc dang ki");
        }
        Role role = roleRepository .findById(userDTO.getRoleID())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));
        //=> convert tuwf DTO sang model
        if (role.getName().toUpperCase().equals("ADMIN")) {
            throw new PermissionDenyException("You can not register an administrator account");
        }

        User newUser = User
                .builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(phoneNumber)
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .build();
        newUser.setRole(role);
        if(userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0){
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
        }
        return userRepository.save(newUser);
        //nen dung Spring + MapStruct sẽ generate code mapping tự động
    }

    @Override
    public String loginUser(String phoneNumber, String password) {
        //security
        Optional<User> optionalUser =  userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()){
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_PHONE_PASSWORD));
        }
        User userExisting = optionalUser.get();
        //check password
        if(userExisting.getFacebookAccountId() == 0 && userExisting.getGoogleAccountId() == 0){
            if(!passwordEncoder.matches(password, userExisting.getPassword())){
                throw new BadCredentialsException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_PHONE_PASSWORD));
            }
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userExisting.getPhoneNumber(), password, userExisting.getAuthorities());

        //authenticate with java spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtils.generateToken(userExisting);
    }

    @Override
    public User getUserById(int id) {
        return null;
    }

    @Override
    public List<User> getAllUser() {
        return List.of();
    }

    @Override
    public User updateUser(long userId, UserDTO userDTO) {
        return null;
    }

    @Override
    public void deleteUser(long userId) {

    }
}
