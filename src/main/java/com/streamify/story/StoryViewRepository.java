package com.streamify.story;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryViewRepository extends JpaRepository<StoryView, String> {
    @Query("""
            SELECT view
            FROM StoryView view
            WHERE view.story.id = :storyId
            """)
    Page<StoryView> findAllStoryViews(Pageable pageable, @Param("storyId") String id);
}
