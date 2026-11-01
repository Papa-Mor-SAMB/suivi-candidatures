package com.papa.suivicandidatures.repository;

import com.papa.suivicandidatures.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
}
