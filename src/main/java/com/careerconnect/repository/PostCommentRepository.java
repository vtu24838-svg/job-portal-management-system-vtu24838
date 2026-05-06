package com.careerconnect.repository;

import com.careerconnect.entity.NetworkPost;
import com.careerconnect.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostOrderByCreatedAtAsc(NetworkPost post);

    long countByPost(NetworkPost post);
}
