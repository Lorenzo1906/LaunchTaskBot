package com.lorenzo.LaunchTaskBot.data.repository;

import com.lorenzo.LaunchTaskBot.data.model.Audit;
import org.springframework.data.repository.CrudRepository;

public interface AuditRepository extends CrudRepository<Audit, Long> {
}
