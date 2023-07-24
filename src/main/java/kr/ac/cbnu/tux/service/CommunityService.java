package kr.ac.cbnu.tux.service;

import jakarta.transaction.Transactional;
import kr.ac.cbnu.tux.domain.Community;
import kr.ac.cbnu.tux.domain.User;
import kr.ac.cbnu.tux.enums.CommunityPostType;
import kr.ac.cbnu.tux.repository.CommunityRepository;
import kr.ac.cbnu.tux.utility.Sanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final Sanitizer sanitizer;

    @Autowired
    public CommunityService(CommunityRepository communityRepository, Sanitizer sanitizer) {
        this.communityRepository = communityRepository;
        this.sanitizer = sanitizer;
    }

    @Transactional
    public void create(Community post, User user, CommunityPostType type) {
        post.setCategory(type);
        post.setBody(sanitizer.sanitize(post.getBody()));
        post.setCreatedDate(OffsetDateTime.now());
        post.setIsDeleted(false);
        post.setView(0L);
        post.setUser(user);
        communityRepository.save(post);
    }

    @Transactional
    public void update(Long id, Community updated, User user) throws Exception {
        Optional<Community> opPost = communityRepository.findById(id);
        if (opPost.isPresent()) {
            Community post = opPost.get();
            if (!Objects.equals(post.getUser().getId(), user.getId())) {
                throw new Exception("User not matched");
            }

            if (updated.getTitle() != null) {
                post.setTitle(updated.getTitle());
            }
            if (updated.getBody() != null) {
                post.setBody(sanitizer.sanitize(updated.getBody()));
            }
            post.setEditedDate(OffsetDateTime.now());
        } else {
            throw new Exception("Not found");
        }
    }

    @Transactional
    public void delete(Long id, User user) throws Exception {
        Optional<Community> opPost = communityRepository.findById(id);
        if (opPost.isPresent()) {
            Community post = opPost.get();
            if (!Objects.equals(post.getUser().getId(), user.getId())) {
                throw new Exception("User not matched");
            }

            post.setIsDeleted(true);
            post.setDeletedDate(OffsetDateTime.now());
        } else {
            throw new Exception("Not found");
        }
    }

    public Optional<Community> read(Long id) {
        communityRepository.updateViewById(id);
        return communityRepository.findById(id);
    }

    public Page<Community> list(Pageable pageable) {
        return communityRepository.findByIsDeletedFalseOrderByCreatedDateDesc(pageable);
    }

    public Page<Community> searchList(String query, Pageable pageable) {
        return communityRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseOrderByCreatedDateDesc(query, pageable);
    }

    public Page<Community> listByCategory(Pageable pageable, CommunityPostType type) {
        return communityRepository.findByIsDeletedFalseAndCategoryOrderByCreatedDateDesc(type, pageable);
    }

    public Page<Community> searchListByCategory(String query, Pageable pageable, CommunityPostType type) {
        return communityRepository.findByIsDeletedFalseAndTitleContainingIgnoreCaseAndCategoryOrderByCreatedDateDesc(query, type, pageable);
    }

    public List<Community> listAll() {
        return communityRepository.findAllByIsDeletedFalse();
    }

    public Long count() {
        return communityRepository.countByIsDeletedFalse();
    }

}
