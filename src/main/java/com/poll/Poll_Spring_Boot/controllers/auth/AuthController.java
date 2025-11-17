package com.poll.Poll_Spring_Boot.controllers.auth;

import com.poll.Poll_Spring_Boot.dtos.AuthenticationResponse;
import com.poll.Poll_Spring_Boot.dtos.SignupRequest;
import com.poll.Poll_Spring_Boot.services.auth.AuthService;
import com.poll.Poll_Spring_Boot.services.jwt.UserService;
import com.poll.Poll_Spring_Boot.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@RequestBody SignupRequest signupRequest){
        try {
            if (authService.hasUserWithEmail(signupRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User with email already exists");
            }
            var createdUser = authService.createUser(signupRequest);
            if (createdUser == null){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create user");
            }
            var userDetails = userService.userDetailsService().loadUserByUsername(createdUser.getEmail());
            String jwt = jwtUtil.generateToken(userDetails, createdUser.getId());

            AuthenticationResponse authenticationResponse = new AuthenticationResponse();
            authenticationResponse.setJwtToken(jwt);
            authenticationResponse.setName(createdUser.getFirstName() + " " + createdUser.getLastName());

            return ResponseEntity.status(HttpStatus.CREATED).body(authenticationResponse);

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }


}
