package com.innovationhub.backend.repositories;

import com.innovationhub.backend.models.ContentPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentPortfolioRepository extends JpaRepository<ContentPortfolio, Long> {
    Optional<ContentPortfolio> findContentPortfolioByUserId(Long userId);
}
