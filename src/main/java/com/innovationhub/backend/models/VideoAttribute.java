package com.innovationhub.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

//    @Lob
    @Column(columnDefinition = "TEXT")
    private String bestFrames;

    private String videoUrl;

    private String videoName;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "portfolio_id")
    private ContentPortfolio contentPortfolio;
}
