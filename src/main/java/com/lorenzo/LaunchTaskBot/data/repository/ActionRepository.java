package com.lorenzo.LaunchTaskBot.data.repository;

import com.lorenzo.LaunchTaskBot.data.model.Action;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActionRepository extends CrudRepository<Action, Long> {

    List<Action> findByProject_IdEquals(long id);
    List<Action> findByProject_IdEqualsAndEnvironmentLikeIgnoreCase(long id, String environment);
    List<Action> findByProject_IdEqualsAndEnvironmentLikeIgnoreCaseAndServiceLikeIgnoreCase(long id, String environment, String service);

    @Query("select a from Action a where upper(a.name) = upper(?1) and upper(a.service) = upper(?2) and upper(a.environment) = upper(?3) and upper(a.project.slackChannel) = upper(?4)")
    List<Action> findActionByParams(String name, String service, String environment, String slackChannel);


}
