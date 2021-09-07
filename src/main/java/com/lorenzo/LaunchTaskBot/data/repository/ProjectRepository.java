package com.lorenzo.LaunchTaskBot.data.repository;

import com.lorenzo.LaunchTaskBot.data.model.Project;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ProjectRepository extends CrudRepository<Project, Long> {
    @Query("select p from Project p where upper(p.channel.name) like upper(?1)")
    Project findByChannel_NameLikeIgnoreCase(String name);
}
