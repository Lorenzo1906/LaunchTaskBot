package com.lorenzo.LaunchTaskBot.data.repository;

import com.lorenzo.LaunchTaskBot.data.model.Action;
import com.lorenzo.LaunchTaskBot.data.model.Project;
import com.lorenzo.LaunchTaskBot.data.model.Role;
import com.lorenzo.LaunchTaskBot.data.model.RoleProjectAction;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoleProjectActionRepository extends CrudRepository<RoleProjectAction, Long> {
    List<RoleProjectAction> findByRoleAndProject(Role role, Project project);

    List<RoleProjectAction> findByRoleAndProjectAndAction(Role role, Project project, Action action);

}
