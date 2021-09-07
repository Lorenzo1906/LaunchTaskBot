package com.lorenzo.LaunchTaskBot.permissions;

import com.lorenzo.LaunchTaskBot.data.model.Role;
import com.lorenzo.LaunchTaskBot.data.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Permissions {
    private static final Logger LOGGER = LoggerFactory.getLogger(Permissions.class);

    @Autowired
    RoleRepository roleRepository;

    public boolean userHavePermissions(String username, String channelName){
        LOGGER.info("Checking permissions for {} in the channel {}", username, channelName);

        List<Role> userRoles = roleRepository.findByUsers_UsernameLike(username);

        for (Role role : userRoles) {
            String currentChannelName = role.getChannel().getName();

            if (currentChannelName.equalsIgnoreCase("all") || currentChannelName.equalsIgnoreCase(channelName)) {
                LOGGER.debug("Permissions found  for {} in the channel {}", username, channelName);

                return true;
            }
        }

        LOGGER.debug("Permissions not found  for {} in the channel {}", username, channelName);
        return false;
    }
}
