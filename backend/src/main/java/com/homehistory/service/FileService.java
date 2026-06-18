package com.homehistory.service;

import jakarta.annotation.PostConstruct;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileService {

    @Value("${app.storage.path:./storage}")
    private String storagePath;

    @Value("${app.thumbnail.width:400}")
    private int thumbnailWidth;

    private Path photoDir;
    private Path videoDir;
    private Path thumbDir;

    @PostConstruct
    public void init() throws IOException {
        Path base = Path.of(storagePath);
        if (!base.isAbsolute()) {
            base = Path.of(System.getProperty("user.home"), "home-history-data").toAbsolutePath();
        }
        photoDir = base.resolve("photos");
        videoDir = base.resolve("videos");
        thumbDir = base.resolve("thumbnails");
        Files.createDirectories(photoDir);
        Files.createDirectories(videoDir);
        Files.createDirectories(thumbDir);
    }

    public String storeFile(MultipartFile file, boolean isVideo) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = String.format("%d/%02d", now.getYear(), now.getMonthValue());
        Path targetDir = isVideo ? videoDir.resolve(yearMonth) : photoDir.resolve(yearMonth);
        Files.createDirectories(targetDir);

        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + ext;
        Path targetPath = targetDir.resolve(filename);

        file.transferTo(targetPath.toFile());
        return targetPath.toString();
    }

    public String generateThumbnail(String filePath, boolean isVideo) throws IOException {
        Path source = Path.of(filePath);
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = String.format("%d/%02d", now.getYear(), now.getMonthValue());
        Path thumbTargetDir = thumbDir.resolve(yearMonth);
        Files.createDirectories(thumbTargetDir);

        String thumbFilename = source.getFileName().toString().replaceFirst("\\.[^.]+$", "_thumb.jpg");
        Path thumbPath = thumbTargetDir.resolve(thumbFilename);

        if (isVideo) {
            generateVideoThumbnail(filePath, thumbPath.toString());
        } else {
            Thumbnails.of(source.toFile())
                    .width(thumbnailWidth)
                    .outputFormat("jpg")
                    .toFile(thumbPath.toFile());
        }
        return thumbPath.toString();
    }

    private void generateVideoThumbnail(String videoPath, String outputPath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-i", videoPath, "-ss", "00:00:01", "-vframes", "1",
                "-vf", "scale=" + thumbnailWidth + ":-1", outputPath, "-y"
        );
        try {
            Process process = pb.start();
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Video thumbnail generation failed", e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException ignored) {}
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}
