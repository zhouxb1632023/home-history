package com.homehistory.controller;

import com.homehistory.dto.CalendarDay;
import com.homehistory.dto.MediaResponse;
import com.homehistory.model.Media;
import com.homehistory.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.*;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<List<MediaResponse>> upload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
            @RequestParam(value = "takenAt", required = false) String takenAt) {
        return ResponseEntity.ok(mediaService.upload(files, description, tagIds, takenAt));
    }

    @GetMapping
    public ResponseEntity<Page<MediaResponse>> search(
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mediaService.search(tagId, startDate, endDate, keyword, type, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getById(id));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getFile(@PathVariable Long id) {
        try {
            Media media = mediaService.getMediaEntity(id);
            Resource resource = new UrlResource(Path.of(media.getFilePath()).toUri());
            MediaTypeFactory.getMediaType(resource).ifPresent(mt -> {}); // ensure it exists
            return ResponseEntity.ok()
                    .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(@PathVariable Long id) {
        try {
            Media media = mediaService.getMediaEntity(id);
            Resource resource = new UrlResource(Path.of(media.getThumbnailPath()).toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarDay>> getCalendar(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(mediaService.getCalendar(year, month));
    }

    @PutMapping("/{id}/description")
    public ResponseEntity<MediaResponse> updateDescription(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(mediaService.updateDescription(id, body.get("description")));
    }

    @PutMapping("/{id}/tags")
    public ResponseEntity<MediaResponse> updateTags(@PathVariable Long id, @RequestBody List<Long> tagIds) {
        return ResponseEntity.ok(mediaService.updateTags(id, tagIds));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
