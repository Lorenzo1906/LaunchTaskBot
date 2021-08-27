package com.lorenzo.LaunchTaskBot.command;

import com.lorenzo.LaunchTaskBot.data.model.Action;
import com.lorenzo.LaunchTaskBot.data.model.Channel;
import com.lorenzo.LaunchTaskBot.data.repository.ActionRepository;
import com.lorenzo.LaunchTaskBot.data.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CommandActions {

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    ChannelRepository channelRepository;

    public boolean executeCommand(Command command) {
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public Command getCommandInfo(Command command, String channelName) {
        List<Action> actions = actionRepository.findActionByParams(command.getAction(), command.getService(), command.getEnvironment(), channelName);

        if (actions.size() != 1){
            return null;
        }

        Action action = actions.get(0);

        command.setEnvironment(action.getEnvironment());
        command.setAction(action.getName());
        command.setService(action.getService());
        command.setUrl(action.getUrl());
        command.setProject(action.getProject().getName());

        return command;
    }
}
