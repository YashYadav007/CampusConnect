package com.campusconnect.user.repository;

import com.campusconnect.enums.RoleName;
import com.campusconnect.user.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(RoleName name);
}
