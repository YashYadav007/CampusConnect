package com.campusconnect.config;

import com.campusconnect.enums.RoleName;
import com.campusconnect.user.entity.Role;
import com.campusconnect.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final RoleRepository roleRepository;

  @Override
  public void run(String... args) {
    ensureRole(RoleName.ROLE_STUDENT);
    ensureRole(RoleName.ROLE_ADMIN);
  }

  private void ensureRole(RoleName name) {
    roleRepository.findByName(name).orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
  }
}
