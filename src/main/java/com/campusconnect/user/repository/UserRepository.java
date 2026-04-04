package com.campusconnect.user.repository;

import com.campusconnect.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  long countByIsActiveTrue();

  @EntityGraph(attributePaths = {"roles"})
  List<User> findAllByOrderByIdDesc();
}
