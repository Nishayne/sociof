package com.hashedin.huSpark.repository;

import com.hashedin.huSpark.model.SharedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SharedPostRepository extends JpaRepository<SharedPost,Long> {
    List<SharedPost> findByUserId(Long userId);
}
