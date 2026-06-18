package com.homehistory.repository;

import com.homehistory.model.MediaTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MediaTagRepository extends JpaRepository<MediaTag, Long> {
    List<MediaTag> findByMediaId(Long mediaId);
    void deleteByMediaId(Long mediaId);
}
