package com.lorenzo.LaunchTaskBot.command;

import com.lorenzo.LaunchTaskBot.data.model.Action;
import com.lorenzo.LaunchTaskBot.data.model.Project;
import com.lorenzo.LaunchTaskBot.data.repository.ActionRepository;
import com.lorenzo.LaunchTaskBot.data.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class CommandActions {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandActions.class);

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public boolean executeCommand(Command command) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public List<String> getEnvByProject(long projectId) {
        Set<String> values = new HashSet<>();
        List<Action> actions = actionRepository.findByProject_IdEquals(projectId);

        for (Action action: actions) {
            values.add(action.getEnvironment());
        }

        return List.copyOf(values);
    }

    public List<String> getServicesByProjectAndEnvironment(long projectId, String env) {
        Set<String> values = new HashSet<>();
        List<Action> actions = actionRepository.findByProject_IdEqualsAndEnvironmentLikeIgnoreCase(projectId, env);

        for (Action action: actions) {
            values.add(action.getService());
        }

        return List.copyOf(values);
    }

    public List<String> getActionsByProjectAndEnvironmentAndService(long projectId, String env, String service) {
        Set<String> values = new HashSet<>();
        List<Action> actions = actionRepository.findByProject_IdEqualsAndEnvironmentLikeIgnoreCaseAndServiceLikeIgnoreCase(projectId, env, service);

        for (Action action: actions) {
            values.add(action.getName());
        }

        return List.copyOf(values);
    }

    public Project getProjectByChannelName(String channelName) {
        return projectRepository.findByChannel_NameLikeIgnoreCase(channelName);
    }

    public Command getCommandInfo(Command command, String channelName) {
        LOGGER.info("Getting command info for channel {}", channelName);

        List<Action> actions = actionRepository.findActionByParams(command.getAction(), command.getService(), command.getEnvironment(), channelName);

        if (actions.size() != 1){
            LOGGER.debug("Not command info found for channel {}", channelName);
            return null;
        }

        Action action = actions.get(0);

        command.setEnvironment(action.getEnvironment());
        command.setAction(action.getName());
        command.setService(action.getService());
        command.setUrl(action.getUrl());
        command.setProject(action.getProject().getName());

        LOGGER.debug("Command info found for channel {}", channelName);

        return command;
    }
}
