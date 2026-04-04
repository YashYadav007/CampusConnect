package com.campusconnect.qa.repository;

import com.campusconnect.qa.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
  Optional<Tag> findByName(String name);
}
