package com.nirmala.hospital.repository;

import com.nirmala.hospital.model.Blog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends MongoRepository<Blog, String> {
    Optional<Blog> findBySlug(String slug);
    List<Blog> findByStatus(String status);
    List<Blog> findByStatusAndCategory(String status, String category);
    
    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?0, $options: 'i' } } ], 'status': 'PUBLISHED' }")
    List<Blog> searchActiveBlogs(String query);
}
