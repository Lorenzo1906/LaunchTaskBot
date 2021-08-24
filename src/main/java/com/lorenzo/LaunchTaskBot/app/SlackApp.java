package com.lorenzo.LaunchTaskBot.app;

import com.lorenzo.LaunchTaskBot.command.Command;
import com.lorenzo.LaunchTaskBot.command.CommandParser;
import com.lorenzo.LaunchTaskBot.data.model.User;
import com.lorenzo.LaunchTaskBot.data.repository.UserRepository;
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
    UserRepository userRepository;

    @Bean
    public App initSlackApp() {
        App app = new App();

        app.command("/action", (req, ctx) -> {
            String value = req.getPayload().getText();

            Command command = CommandParser.parse(value.split(" "));

            return ctx.ack(asBlocks(
                section(section -> section.text(markdownText(":wave: pong"))),
                actions(actions -> actions
                    .elements(asElements(
                        button(b -> b.actionId("confirm-action").style("primary").text(plainText(pt -> pt.text("Accept"))).value(value)),
                        button(b -> b.actionId("cancel-action").style("danger").text(plainText(pt -> pt.text("Cancel"))).value(value))
                    ))
                )
            ));
        });

        app.blockAction("confirm-action", (req, ctx) -> {
            String value = req.getPayload().getActions().get(0).getValue();

            if (req.getPayload().getResponseUrl() != null) {
                ctx.ack();
                ctx.client().chatPostMessage(r -> r
                    .channel(req.getPayload().getChannel().getName())
                    .text("Yikes")
                );
            }
            return ctx.ack();
        });
        app.blockAction("cancel-action", (req, ctx) -> {
            String value = req.getPayload().getActions().get(0).getValue();
            if (req.getPayload().getResponseUrl() != null) {
                ctx.respond("You've sent " + value + " by clicking the button! - ");
            }
            return ctx.ack();
        });

        return app;
    }
}