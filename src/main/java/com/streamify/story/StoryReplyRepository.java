package com.streamify.story;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryReplyRepository extends JpaRepository<StoryReply, String> {
    @Query("""
            SELECT reply
            FROM StoryReply reply
            WHERE reply.story.id = :storyId
            """)
    Page<StoryReply> findAllStoryReplies(Pageable pageable, @Param("storyId") String storyId);
}
