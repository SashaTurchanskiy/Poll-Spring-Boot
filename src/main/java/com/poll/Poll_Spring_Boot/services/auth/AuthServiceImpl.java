package com.poll.Poll_Spring_Boot.services.auth;

import com.poll.Poll_Spring_Boot.dtos.SignupRequest;
import com.poll.Poll_Spring_Boot.dtos.UserDto;
import com.poll.Poll_Spring_Boot.enums.UserRole;
import com.poll.Poll_Spring_Boot.model.User;
import com.poll.Poll_Spring_Boot.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(SignupRequest signupRequest) {
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setUserRole(UserRole.USER);
        User createdUser = userRepo.save(user);
        return createdUser.getUserDto();
    }

    @Override
    public Boolean hasUserWithEmail(String email) {
        return userRepo.findFirstByEmail(email).isPresent();
    }
}
