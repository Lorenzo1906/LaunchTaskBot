package com.lorenzo.LaunchTaskBot.permissions;

import com.lorenzo.LaunchTaskBot.data.model.Role;
import com.lorenzo.LaunchTaskBot.data.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Permissions {

    @Autowired
    RoleRepository roleRepository;

    public boolean userHavePermissions(String username, String channelName){
        List<Role> userRoles = roleRepository.findByUsers_UsernameLike(username);

        for (Role role : userRoles) {
            String currentChannelName = role.getChannel().getName();

            if (currentChannelName.equalsIgnoreCase("all") || currentChannelName.equalsIgnoreCase(channelName)) {
                return true;
            }
        }

        return false;
    }
}
