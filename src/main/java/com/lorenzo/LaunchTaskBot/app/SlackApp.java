package com.lorenzo.LaunchTaskBot.app;

import com.lorenzo.LaunchTaskBot.command.CommandParser;
import com.slack.api.bolt.App;
import com.slack.api.model.block.composition.OptionObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;

@Configuration
public class SlackApp {

    @Bean
    public App initSlackApp() {
        CommandParser parser = new CommandParser();
        App app = new App();

        OptionObject optionObject = new OptionObject();
        optionObject.setText(plainText(pt -> pt.text("Ping2")));

        app.command("/hello", (req, ctx) -> ctx.ack(asBlocks(
            section(section -> section.text(markdownText(":wave: pong"))),
            actions(actions -> actions
                .elements(asElements(
                        button(b -> b.actionId("ping-again").text(plainText(pt -> pt.text("Ping"))).value("ping")),
                        button(b -> b.actionId("ping-again2").text(plainText(pt -> pt.text("Ping2"))).value("ping2")),
                        overflowMenu(m -> m.actionId("fsdf").options(asOptions(optionObject)))
                ))
            )
        )));

        app.blockAction("ping-again", (req, ctx) -> {
            String value = req.getPayload().getActions().get(0).getValue();
            if (req.getPayload().getResponseUrl() != null) {
                ctx.respond("You've sent " + value + " by clicking the button!");
                ctx.client().chatPostMessage(r -> r
                    .channel(req.getPayload().getChannel().getName())
                    .text("Yikes")
                );
            }
            return ctx.ack();
        });
        app.blockAction("/ping-again2", (req, ctx) -> {
            String value = req.getPayload().getActions().get(0).getValue();
            if (req.getPayload().getResponseUrl() != null) {
                return ctx.ack();
            }
            return ctx.ack();
        });

        return app;
    }
}