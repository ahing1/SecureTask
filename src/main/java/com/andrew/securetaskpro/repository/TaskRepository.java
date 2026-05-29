package com.andrew.securetaskpro.repository;

import com.andrew.securetaskpro.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByOrganizationId(Long organizatoinId, Pageable pageable);
    Optional<Task> findByIdAndOrganizationId(Long id, Long organizationId);
}
