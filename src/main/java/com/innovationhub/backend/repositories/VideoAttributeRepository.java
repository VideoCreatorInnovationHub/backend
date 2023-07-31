package com.innovationhub.backend.repositories;

import com.innovationhub.backend.models.VideoAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface VideoAttributeRepository extends JpaRepository<VideoAttribute, Long> {

}
