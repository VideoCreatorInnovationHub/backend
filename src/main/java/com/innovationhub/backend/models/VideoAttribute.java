package com.innovationhub.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoAttribute {
    @Id
    private long id;

    @ElementCollection
    private List<String> bestFrames;

    private String videoUrl;

    @ManyToOne
    @JoinColumn(name = "content_portfolio_id", nullable = false)
    private ContentPortfolio contentPortfolio;
}
