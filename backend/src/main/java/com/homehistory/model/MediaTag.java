package com.homehistory.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "media_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
