package com.lorenzo.LaunchTaskBot.command;

import com.lorenzo.LaunchTaskBot.data.model.Action;
import com.lorenzo.LaunchTaskBot.data.model.Project;
import com.lorenzo.LaunchTaskBot.data.repository.ActionRepository;
import com.lorenzo.LaunchTaskBot.data.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
        List<Action> actions = actionRepository.findActionByParams(command.getAction(), command.getService(), command.getEnvironment(), command.getProject());

        if (actions.size() > 1) {
            LOGGER.error("More than one action returned for current configuration");

            return false;
        } else if (actions.size() < 1) {
            LOGGER.error("No action returned for current configuration");

            return false;
        } else {
            LOGGER.debug("Action found, executing action.");
        }

        String credentials = System.getenv("BAMBOO_CREDENTIALS");
        if (credentials == null || credentials.isBlank()) {
            LOGGER.error("Bamboo credentials not found");

            return false;
        } else {
            LOGGER.debug("Bamboo credentials found");

            try {
                Action action = actions.get(0);

                URL url = new URL(action.getUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization ", "Bearer " + credentials);

                if (conn.getResponseCode() != 200) {
                    LOGGER.error("Failed : HTTP error code : {}", conn.getResponseCode());
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                    String output;
                    LOGGER.debug("Output from Server ....");
                    while ((output = br.readLine()) != null) {

                        System.out.println(output);
                    }

                    conn.disconnect();
                }
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
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
        return projectRepository.findBySlackChannelIgnoreCase(channelName);
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
