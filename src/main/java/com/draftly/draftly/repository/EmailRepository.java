package com.draftly.draftly.repository;


import com.draftly.draftly.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, String> {
}
