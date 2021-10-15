package com.lorenzo.LaunchTaskBot.app;

import com.lorenzo.LaunchTaskBot.command.Command;
import com.lorenzo.LaunchTaskBot.command.CommandActions;
import com.lorenzo.LaunchTaskBot.command.CommandParser;
import com.lorenzo.LaunchTaskBot.data.model.Project;
import com.lorenzo.LaunchTaskBot.permissions.Permissions;
import com.slack.api.bolt.App;
import com.slack.api.model.block.composition.OptionObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;

@Configuration
public class SlackApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackApp.class);

    @Autowired
    Permissions permissions;

    @Autowired
    CommandActions commandActions;

    @Autowired
    CommandParser commandParser;

    @Bean
    public App initSlackApp() {
        App app = new App();

        app.command("/launchbot", (req, ctx) -> {
            LOGGER.info("Received command in the channel {}", req.getPayload().getChannelName());

            if (permissions.userHavePermissionsToChannel(req.getPayload().getUserName(), req.getPayload().getChannelName())) {
                LOGGER.debug("Permissions validated for user {}", req.getPayload().getUserName());

                Project project = commandActions.getProjectByChannelName(req.getPayload().getChannelName());

                if (project != null) {
                    LOGGER.debug("Command validated successfully, generating answer");

                    List<OptionObject> options = new ArrayList<>();

                    List<String> values = commandActions.getEnvByProject(project.getId());
                    for (String value : values) {
                        options.add(option(plainText(value), "-e " + value));
                    }

                    return ctx.ack(asBlocks(
                        section(section -> section.text(markdownText("*Do you want to execute the following action?*"))),
                        section(section -> section.text(markdownText("Project: " + project.getName()))),
                        section(section -> section.text(markdownText("Environment: TBD"))),
                        section(section -> section.text(markdownText("Service: TBD"))),
                        section(section -> section.text(markdownText("Action: TBD"))),
                        section(section -> section
                            .blockId("category-block")
                            .text(markdownText("Select an environment"))
                            .accessory(staticSelect(staticSelect -> staticSelect
                                .actionId("environment-selection-action")
                                .placeholder(plainText("Select an environment"))
                                .options(options)
                            )
                        )),
                        actions(actions -> actions
                            .elements(asElements(
                                button(b -> b.actionId("cancel-action").style("danger").text(plainText(pt -> pt.text("Cancel"))).value("cancel"))
                            ))
                        )
                    ));
                } else {
                    LOGGER.debug("Invalid information in command, generating answer");

                    return ctx.ack(asBlocks(
                        section(section -> section.text(markdownText("*No Action found for this channel*")))
                    ));
                }
            } else {
                LOGGER.debug("User with invalid permissions {}", req.getPayload().getUserName());

                return ctx.ack(asBlocks(
                    section(section -> section.text(markdownText("*You do not have permissions to do this action*")))
                ));
            }
        });

        app.blockAction("confirm-action", (req, ctx) -> {
            LOGGER.info("Received action confirmation in the channel {}", req.getPayload().getChannel().getName());

            String value = req.getPayload().getActions().get(0).getValue();
            Command command = commandParser.parse(value.split(" "));
            command = commandActions.getCommandInfo(command, req.getPayload().getChannel().getName());

            if (permissions.userHavePermissionsToAction(command, req.getPayload().getUser().getUsername(), req.getPayload().getChannel().getName())) {
                if (req.getPayload().getResponseUrl() != null) {
                    LOGGER.debug("Generating channel notification {}", req.getPayload().getChannel().getName());

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

                    LOGGER.debug("Start action execution");
                    boolean result = commandActions.executeCommand(command);

                    if (result) {
                        LOGGER.debug("Action executed correctly, generating answer");

                        ctx.client().chatPostMessage(r -> r
                                .channel(req.getPayload().getChannel().getName())
                                .text(":white_check_mark: Execution successful")
                        );
                    } else {
                        LOGGER.debug("Error while executing action, generating answer");

                        ctx.client().chatPostMessage(r -> r
                                .channel(req.getPayload().getChannel().getName())
                                .text(":x: Error executing action")
                        );
                    }
                }
            } else {
                LOGGER.debug("User with invalid permissions {}", req.getPayload().getUser().getUsername());

                ctx.respond(r -> r
                        .replaceOriginal(true)
                        .text("*You do not have permissions to do this action*")
                );
            }

            return ctx.ack();
        });

        app.blockAction("cancel-action", (req, ctx) -> {
            LOGGER.info("Received command to cancel action in the channel {}", req.getPayload().getChannel().getName());

            if (req.getPayload().getResponseUrl() != null) {
                ctx.respond(r -> r
                    .replaceOriginal(true)
                    .text(":x: Action execution cancelled")
                );
            }
            return ctx.ack();
        });

        app.blockAction("environment-selection-action", (req, ctx) -> {
            if (req.getPayload().getResponseUrl() != null) {
                String value = req.getPayload().getActions().get(0).getSelectedOption().getValue();

                Command command = commandParser.parse(value.split(" "));
                Project project = commandActions.getProjectByChannelName(req.getPayload().getChannel().getName());

                if (!command.getEnvironment().isEmpty() && project != null) {

                    List<OptionObject> options = new ArrayList<>();

                    List<String> values = commandActions.getServicesByProjectAndEnvironment(project.getId(), command.getEnvironment());
                    for (String str : values) {
                        options.add(option(plainText(str), value + " -s " + str));
                    }

                    ctx.respond(r -> r
                        .replaceOriginal(true)
                        .blocks(asBlocks(
                            section(section -> section.text(markdownText("*Do you want to execute the following action?*"))),
                            section(section -> section.text(markdownText("Project: " + project.getName()))),
                            section(section -> section.text(markdownText("Environment: " + command.getEnvironment()))),
                            section(section -> section.text(markdownText("Service: TBD"))),
                            section(section -> section.text(markdownText("Action: TBD"))),
                            section(section -> section
                                .blockId("category-block")
                                .text(markdownText("Select a service"))
                                .accessory(staticSelect(staticSelect -> staticSelect
                                    .actionId("service-selection-action")
                                    .placeholder(plainText("Select a service"))
                                    .options(options)
                                ))),
                            actions(actions -> actions
                                .elements(asElements(
                                    button(b -> b.actionId("cancel-action").style("danger").text(plainText(pt -> pt.text("Cancel"))).value("cancel"))
                                ))
                            )
                        ))
                    );
                }
            }
            return ctx.ack();
        });

        app.blockAction("service-selection-action", (req, ctx) -> {
            if (req.getPayload().getResponseUrl() != null) {
                String value = req.getPayload().getActions().get(0).getSelectedOption().getValue();

                Command command = commandParser.parse(value.split(" "));
                Project project = commandActions.getProjectByChannelName(req.getPayload().getChannel().getName());

                if (!command.getService().isEmpty() && !command.getEnvironment().isEmpty() && project != null) {

                    List<OptionObject> options = new ArrayList<>();

                    List<String> values = commandActions.getActionsByProjectAndEnvironmentAndService(project.getId(), command.getEnvironment(), command.getService());
                    for (String str : values) {
                        options.add(option(plainText(str), value + " -a " + str));
                    }

                    ctx.respond(r -> r
                        .replaceOriginal(true)
                        .blocks(asBlocks(
                            section(section -> section.text(markdownText("*Do you want to execute the following action?*"))),
                            section(section -> section.text(markdownText("Project: " + project.getName()))),
                            section(section -> section.text(markdownText("Environment: " + command.getEnvironment()))),
                            section(section -> section.text(markdownText("Service: " + command.getService()))),
                            section(section -> section.text(markdownText("Action: TBD"))),
                            section(section -> section
                                .blockId("category-block")
                                .text(markdownText("Select an action"))
                                .accessory(staticSelect(staticSelect -> staticSelect
                                    .actionId("action-selection-action")
                                    .placeholder(plainText("Select an action"))
                                    .options(options)
                                ))
                            ),
                            actions(actions -> actions
                                .elements(asElements(
                                    button(b -> b.actionId("cancel-action").style("danger").text(plainText(pt -> pt.text("Cancel"))).value("cancel"))
                                ))
                            )
                        ))
                    );
                }
            }
            return ctx.ack();
        });

        app.blockAction("action-selection-action", (req, ctx) -> {
            if (req.getPayload().getResponseUrl() != null) {
                String value = req.getPayload().getActions().get(0).getSelectedOption().getValue();

                Command command = commandParser.parse(value.split(" "));
                Project project = commandActions.getProjectByChannelName(req.getPayload().getChannel().getName());

                if (!command.getAction().isEmpty() && !command.getService().isEmpty() && !command.getEnvironment().isEmpty() && project != null) {
                    ctx.respond(r -> r
                        .replaceOriginal(true)
                        .blocks(asBlocks(
                            section(section -> section.text(markdownText("*Do you want to execute the following action?*"))),
                            section(section -> section.text(markdownText("Project: " + project.getName()))),
                            section(section -> section.text(markdownText("Environment: " + command.getEnvironment()))),
                            section(section -> section.text(markdownText("Service: " + command.getService()))),
                            section(section -> section.text(markdownText("Action: " + command.getAction()))),
                            actions(actions -> actions
                                .elements(asElements(
                                    button(b -> b.actionId("confirm-action").style("primary").text(plainText(pt -> pt.text("Accept"))).value(value)),
                                    button(b -> b.actionId("cancel-action").style("danger").text(plainText(pt -> pt.text("Cancel"))).value("cancel"))
                                ))
                            )
                        ))
                    );
                }
            }
            return ctx.ack();
        });

        return app;
    }
}