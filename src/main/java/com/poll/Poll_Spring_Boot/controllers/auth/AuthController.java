package com.poll.Poll_Spring_Boot.controllers.auth;

import com.poll.Poll_Spring_Boot.dtos.AuthenticationRequest;
import com.poll.Poll_Spring_Boot.dtos.AuthenticationResponse;
import com.poll.Poll_Spring_Boot.dtos.SignupRequest;
import com.poll.Poll_Spring_Boot.model.User;
import com.poll.Poll_Spring_Boot.repositories.UserRepo;
import com.poll.Poll_Spring_Boot.services.auth.AuthService;
import com.poll.Poll_Spring_Boot.services.jwt.UserService;
import com.poll.Poll_Spring_Boot.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;

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

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthenticationRequest authenticationRequest){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
            );

            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(authenticationRequest.getEmail());
            Optional<User> optionalUser = userRepo.findFirstByEmail(userDetails.getUsername());

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                String jwt = jwtUtil.generateToken(userDetails, user.getId());

                AuthenticationResponse authenticationResponse = new AuthenticationResponse();
                authenticationResponse.setJwtToken(jwt);
                authenticationResponse.setName(user.getFirstName() + " " + user.getLastName());

                return ResponseEntity.ok(authenticationResponse);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "User not found"));

        }catch (BadCredentialsException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid email or password"));
        }catch (DisabledException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("message", "User account is disabled"));
        }catch (UsernameNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "User not found"));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
