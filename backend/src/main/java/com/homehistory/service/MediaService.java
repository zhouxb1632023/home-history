package com.homehistory.service;

import com.homehistory.dto.CalendarDay;
import com.homehistory.dto.MediaResponse;
import com.homehistory.model.*;
import com.homehistory.model.enums.MediaType;
import com.homehistory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaTagRepository mediaTagRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow();
    }

    public Media getMediaEntity(Long id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));
    }

    @Transactional
    public List<MediaResponse> upload(List<MultipartFile> files, String description,
                                       List<Long> tagIds, String takenAtStr) {
        User user = getCurrentUser();
        LocalDateTime takenAt = takenAtStr != null && !takenAtStr.isEmpty()
                ? LocalDateTime.parse(takenAtStr)
                : LocalDateTime.now();

        return files.stream().map(file -> {
            try {
                boolean isVideo = file.getContentType() != null
                        && file.getContentType().startsWith("video");
                String filePath = fileService.storeFile(file, isVideo);
                String thumbPath = fileService.generateThumbnail(filePath, isVideo);

                Media media = Media.builder()
                        .user(user)
                        .filename(filePath.substring(filePath.lastIndexOf("/") + 1))
                        .originalName(file.getOriginalFilename())
                        .filePath(filePath)
                        .mediaType(isVideo ? MediaType.VIDEO : MediaType.PHOTO)
                        .fileSize(file.getSize())
                        .mimeType(file.getContentType())
                        .thumbnailPath(thumbPath)
                        .takenAt(takenAt)
                        .description(description)
                        .build();
                media = mediaRepository.save(media);

                if (tagIds != null) {
                    for (Long tagId : tagIds) {
                        Media finalMedia = media;
                        tagRepository.findById(tagId).ifPresent(tag ->
                            mediaTagRepository.save(MediaTag.builder().media(finalMedia).tag(tag).build())
                        );
                    }
                }
                return toResponse(media);
            } catch (IOException e) {
                throw new RuntimeException("File upload failed: " + file.getOriginalFilename(), e);
            }
        }).collect(Collectors.toList());
    }

    public Page<MediaResponse> search(Long tagId, String startDate, String endDate,
                                       String keyword, String type, int page, int size) {
        MediaType mediaType = type != null ? MediaType.valueOf(type.toUpperCase()) : null;
        LocalDateTime start = startDate != null && !startDate.isEmpty()
                ? LocalDate.parse(startDate).atStartOfDay() : null;
        LocalDateTime end = endDate != null && !endDate.isEmpty()
                ? LocalDate.parse(endDate).plusDays(1).atStartOfDay() : null;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "takenAt"));
        return mediaRepository.searchMedia(tagId, start, end, keyword, mediaType, pageable)
                .map(this::toResponse);
    }

    public MediaResponse getById(Long id) {
        return mediaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Media not found"));
    }

    @Transactional
    public MediaResponse updateDescription(Long id, String description) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        media.setDescription(description);
        return toResponse(mediaRepository.save(media));
    }

    @Transactional
    public MediaResponse updateTags(Long mediaId, List<Long> tagIds) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        mediaTagRepository.deleteByMediaId(mediaId);
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                tagRepository.findById(tagId).ifPresent(tag ->
                    mediaTagRepository.save(MediaTag.builder().media(media).tag(tag).build())
                );
            }
        }
        return toResponse(media);
    }

    @Transactional
    public void delete(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        fileService.deleteFile(media.getFilePath());
        Optional.ofNullable(media.getThumbnailPath()).ifPresent(fileService::deleteFile);
        mediaTagRepository.deleteByMediaId(id);
        mediaRepository.delete(media);
    }

    public List<CalendarDay> getCalendar(int year, int month) {
        return mediaRepository.findDatesWithMedia(year, month).stream()
                .map(date -> new CalendarDay(date.getDayOfMonth(), 1))
                .collect(Collectors.toList());
    }

    private MediaResponse toResponse(Media media) {
        List<MediaResponse.TagDto> tags = mediaTagRepository.findByMediaId(media.getId()).stream()
                .map(mt -> MediaResponse.TagDto.builder()
                        .id(mt.getTag().getId())
                        .name(mt.getTag().getName())
                        .color(mt.getTag().getColor())
                        .build())
                .collect(Collectors.toList());

        return MediaResponse.builder()
                .id(media.getId())
                .originalName(media.getOriginalName())
                .mediaType(media.getMediaType())
                .fileSize(media.getFileSize())
                .mimeType(media.getMimeType())
                .width(media.getWidth())
                .height(media.getHeight())
                .takenAt(media.getTakenAt())
                .description(media.getDescription())
                .uploaderName(media.getUser().getNickname())
                .tags(tags)
                .createdAt(media.getCreatedAt())
                .build();
    }
}