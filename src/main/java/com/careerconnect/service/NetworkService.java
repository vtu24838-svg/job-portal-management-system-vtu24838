package com.careerconnect.service;

import com.careerconnect.dto.NetworkPostDto;
import com.careerconnect.entity.NetworkPost;
import com.careerconnect.entity.PostComment;
import com.careerconnect.entity.User;
import com.careerconnect.repository.NetworkPostRepository;
import com.careerconnect.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NetworkService {

    private final NetworkPostRepository postRepository;
    private final PostCommentRepository commentRepository;

    /** Create a new network post. Only verified users should call this. */
    @Transactional
    public NetworkPost createPost(NetworkPostDto dto, User author) {
        if (!author.isVerified() && author.getRole().name().equals("APPLICANT")) {
            throw new RuntimeException("Only verified professionals can create posts.");
        }
        NetworkPost post = new NetworkPost();
        post.setContent(dto.getContent());
        post.setPostType(dto.getPostType() != null ? dto.getPostType() : com.careerconnect.enums.PostType.UPDATE);
        post.setAuthor(author);
        return postRepository.save(post);
    }

    /** Toggle like on a post (simple increment — no de-dup for brevity) */
    @Transactional
    public int likePost(Long postId) {
        NetworkPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);
        return post.getLikes();
    }

    /** Add a comment to a post */
    @Transactional
    public PostComment addComment(Long postId, String content, User author) {
        NetworkPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        PostComment comment = new PostComment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setPost(post);
        return commentRepository.save(comment);
    }

    /** Delete a post — only author or admin */
    @Transactional
    public void deletePost(Long postId, User user) {
        NetworkPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isAuthor && !isAdmin) {
            throw new RuntimeException("Unauthorized");
        }
        postRepository.delete(post);
    }

    /** Get the full feed (newest first) — loads comments eagerly for template rendering */
    @Transactional(readOnly = true)
    public List<NetworkPost> getFeed() {
        List<NetworkPost> posts = postRepository.findAllByOrderByCreatedAtDesc();
        // Force-initialize comments and author to avoid LazyInitializationException in template
        posts.forEach(p -> {
            p.getComments().forEach(c -> {
                c.getAuthor().getName(); // init comment author
            });
            p.getAuthor().getName(); // init post author
        });
        return posts;
    }

    public Optional<NetworkPost> findById(Long id) {
        return postRepository.findById(id);
    }

    public long countAll() {
        return postRepository.count();
    }
}
