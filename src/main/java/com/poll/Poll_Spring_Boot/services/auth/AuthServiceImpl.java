package com.poll.Poll_Spring_Boot.services.auth;

import com.poll.Poll_Spring_Boot.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepo userRepo;

    @Override
    public Boolean hasUserWithEmail(String email) {
        return userRepo.findFirstByEmail(email).isPresent();
    }
}
