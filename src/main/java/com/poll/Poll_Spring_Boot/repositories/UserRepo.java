package com.poll.Poll_Spring_Boot.repositories;

import com.poll.Poll_Spring_Boot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
}
