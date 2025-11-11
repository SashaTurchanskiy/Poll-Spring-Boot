package com.poll.Poll_Spring_Boot.repositories;

import com.poll.Poll_Spring_Boot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

   Optional<User> findFirstByEmail(String email);
}
