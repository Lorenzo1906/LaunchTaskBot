package com.lorenzo.LaunchTaskBot.data.repository;

import com.lorenzo.LaunchTaskBot.data.model.Action;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActionRepository extends CrudRepository<Action, Long> {
    @Query("select a from Action a where upper(a.name) like upper(:name) and upper(a.service) like upper(:service) and upper(a.environment) like upper(:env) and upper(a.channel.name) like upper(:channel)")
    List<Action> findActionByParams(@Param("name") String name, @Param("service") String service, @Param("env") String environment, @Param("channel") String channelName);

    List<Action> findByProject_IdEquals(long id);
    List<Action> findByProject_IdEqualsAndEnvironmentLikeIgnoreCase(long id, String environment);
    List<Action> findByProject_IdEqualsAndEnvironmentLikeIgnoreCaseAndServiceLikeIgnoreCase(long id, String environment, String service);


}
