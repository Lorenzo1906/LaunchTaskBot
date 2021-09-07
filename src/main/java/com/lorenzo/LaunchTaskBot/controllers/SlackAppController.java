package com.lorenzo.LaunchTaskBot.controllers;

import com.slack.api.bolt.App;
import com.slack.api.bolt.servlet.SlackAppServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;

@WebServlet("/slack/events")
public class SlackAppController extends SlackAppServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackAppController.class);

    public SlackAppController(App app) {
        super(app);
        LOGGER.info("Setting up slack app");
    }
}