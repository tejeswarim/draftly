package com.draftly.draftly.repository;

import com.draftly.draftly.entity.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DraftRepository extends JpaRepository<Draft,Long> {
    boolean existsByMessageId(String messageId);
}
