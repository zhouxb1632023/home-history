package com.homehistory.service;

import com.homehistory.dto.TagRequest;
import com.homehistory.model.Tag;
import com.homehistory.repository.TagRepository;
import com.homehistory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public List<Tag> getAll() {
        return tagRepository.findAllByOrderByNameAsc();
    }

    public Tag create(TagRequest req) {
        if (tagRepository.existsByName(req.getName())) {
            throw new RuntimeException("Tag already exists");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username).orElseThrow();

        Tag tag = Tag.builder()
                .name(req.getName())
                .color(req.getColor() != null ? req.getColor() : "#E07B4C")
                .createdBy(user)
                .build();
        return tagRepository.save(tag);
    }

    public void delete(Long id) {
        tagRepository.deleteById(id);
    }
}
