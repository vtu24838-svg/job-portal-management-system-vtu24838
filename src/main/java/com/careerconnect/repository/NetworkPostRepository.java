package com.careerconnect.repository;

import com.careerconnect.entity.NetworkPost;
import com.careerconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkPostRepository extends JpaRepository<NetworkPost, Long> {

    List<NetworkPost> findAllByOrderByCreatedAtDesc();

    List<NetworkPost> findByAuthorOrderByCreatedAtDesc(User author);
}
