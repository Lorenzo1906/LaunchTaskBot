package com.lorenzo.LaunchTaskBot.permissions;

import com.lorenzo.LaunchTaskBot.command.Command;
import com.lorenzo.LaunchTaskBot.data.model.*;
import com.lorenzo.LaunchTaskBot.data.repository.*;
import kotlin.collections.ArrayDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Permissions {
    private static final Logger LOGGER = LoggerFactory.getLogger(Permissions.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    RoleProjectActionRepository roleProjectActionRepository;

    @Autowired
    ActionRepository actionRepository;

    public boolean userHavePermissionsToChannel(String username, String channelName){
        LOGGER.info("Checking permissions for {} in the channel {}", username, channelName);

        User user = userRepository.findOneByUsername(username);
        System.out.println(user.getId() + user.getUsername());
        Project project = projectRepository.findBySlackChannelIgnoreCase(channelName);
        System.out.println(project.getId() + project.getSlackChannel());

        List<RoleProjectAction> roleActions = new ArrayList<>();

        for (Role role : user.getRoles()) {
            roleActions.addAll(roleProjectActionRepository.findByRoleAndProject(role, project));
            System.out.println(role.getName());
        }

        if (roleActions.size() > 0) {
            LOGGER.debug("Permissions found  for {} in the channel {}", username, channelName);

            return true;
        }

        LOGGER.debug("Permissions not found  for {} in the channel {}", username, channelName);
        return false;
    }

    public boolean userHavePermissionsToAction(Command command, String username){
        Project project = projectRepository.findByNameIgnoreCase(command.getProject());
        User user = userRepository.findOneByUsername(username);
        List<Action> actions = actionRepository.findActionByParams(command.getAction(), command.getService(), command.getEnvironment(), command.getProject());

        List<RoleProjectAction> projectActions = new ArrayList<>();
        for (Role role : user.getRoles()) {
            for (Action action : actions) {
                projectActions.addAll(roleProjectActionRepository.findByRoleAndProjectAndAction(role, project, action));
            }
        }

        return projectActions.size() > 0;
    }
}
