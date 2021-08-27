package com.lorenzo.LaunchTaskBot.app;

import com.lorenzo.LaunchTaskBot.command.Command;
import com.lorenzo.LaunchTaskBot.command.CommandActions;
import com.lorenzo.LaunchTaskBot.command.CommandParser;
import com.lorenzo.LaunchTaskBot.permissions.Permissions;
import com.slack.api.bolt.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;

@Configuration
public class SlackApp {

    @Autowired
    Permissions permissions;

    @Autowired
    CommandActions commandActions;

    @Autowired
    CommandParser commandParser;

    @Bean
    public App initSlackApp() {
        App app = new App();

        app.command("/action", (req, ctx) -> {
            String value = req.getPayload().getText();

            if (permissions.userHavePermissions(req.getPayload().getUserName(), req.getPayload().getChannelName())) {
                Command command = commandParser.parse(value.split(" "));
                command = commandActions.getCommandInfo(command, req.getPayload().getChannelName());

                if (command != null) {
                    Command finalCommand = command;
                    return ctx.ack(asBlocks(
                        section(section -> section.text(markdownText("*Do you want to execute the following action?*"))),
                        section(section -> section.text(markdownText("Project: " + finalCommand.getProject()))),
                        section(section -> section.text(markdownText("Service: " + finalCommand.getService()))),
                        section(section -> section.text(markdownText("Environment: " + finalCommand.getEnvironment()))),
                        section(section -> section.text(markdownText("Action: " + finalCommand.getAction()))),
                        actions(actions -> actions
                            .elements(asElements(
                                button(b -> b.actionId("confirm-action").style("primary").text(plainText(pt -> pt.text("Accept"))).value(value)),
                                button(b -> b.actionId("cancel-action").style("danger").text(plainText(pt -> pt.text("Cancel"))).value(value))
                            ))
                        )
                    ));
                } else {
                    return ctx.ack(asBlocks(
                        section(section -> section.text(markdownText("*No Action found for this channel*")))
                    ));
                }
            } else {
                return ctx.ack(asBlocks(
                    section(section -> section.text(markdownText("*You do not have permissions to do this action*")))
                ));
            }
        });

        app.blockAction("confirm-action", (req, ctx) -> {
            String value = req.getPayload().getActions().get(0).getValue();
            Command command = commandParser.parse(value.split(" "));
            command = commandActions.getCommandInfo(command, req.getPayload().getChannel().getName());

            if (req.getPayload().getResponseUrl() != null) {
                Command finalCommand = command;

                ctx.respond(r -> r
                    .replaceOriginal(true)
                    .text(":white_check_mark: Action confirmed")
                );
                ctx.client().chatPostMessage(r -> r
                    .channel(req.getPayload().getChannel().getName())
                    .blocks(asBlocks(
                        section(section -> section.text(markdownText(":clock1: *Executing action*"))),
                        section(section -> section.text(markdownText("Project: " + finalCommand.getProject()))),
                        section(section -> section.text(markdownText("Service: " + finalCommand.getService()))),
                        section(section -> section.text(markdownText("Environment: " + finalCommand.getEnvironment()))),
                        section(section -> section.text(markdownText("Action: " + finalCommand.getAction())))
                    ))
                );

                boolean result = commandActions.executeCommand(command);
                if (result) {
                    ctx.client().chatPostMessage(r -> r
                        .channel(req.getPayload().getChannel().getName())
                        .text(":white_check_mark: Execution successful")
                    );
                } else {
                    ctx.client().chatPostMessage(r -> r
                        .channel(req.getPayload().getChannel().getName())
                        .text(":x: Error executing action")
                    );
                }
            }

            return ctx.ack();
        });

        app.blockAction("cancel-action", (req, ctx) -> {
            if (req.getPayload().getResponseUrl() != null) {
                ctx.respond(r -> r
                    .replaceOriginal(true)
                    .text(":x: Action execution cancelled")
                );
            }
            return ctx.ack();
        });

        return app;
    }
}