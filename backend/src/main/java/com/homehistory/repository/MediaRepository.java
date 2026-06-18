package com.homehistory.repository;

import com.homehistory.model.Media;
import com.homehistory.model.enums.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query("SELECT DISTINCT m FROM Media m " +
           "LEFT JOIN MediaTag mt ON mt.media = m " +
           "WHERE (:tagId IS NULL OR mt.tag.id = :tagId) " +
           "AND (:startDate IS NULL OR m.takenAt >= :startDate) " +
           "AND (:endDate IS NULL OR m.takenAt < :endDate) " +
           "AND (:keyword IS NULL OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:type IS NULL OR m.mediaType = :type) " +
           "ORDER BY m.takenAt DESC")
    Page<Media> searchMedia(@Param("tagId") Long tagId,
                            @Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate,
                            @Param("keyword") String keyword,
                            @Param("type") MediaType type,
                            Pageable pageable);

    @Query("SELECT DISTINCT FUNCTION('DATE', m.takenAt) FROM Media m " +
           "WHERE YEAR(m.takenAt) = :year AND MONTH(m.takenAt) = :month")
    List<java.time.LocalDate> findDatesWithMedia(@Param("year") int year, @Param("month") int month);
}