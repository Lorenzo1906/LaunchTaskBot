package com.lorenzo.LaunchTaskBot.command;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CommandParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandParser.class);

    public Command parse(String[] args) {
        LOGGER.debug("Parsing command text {}", (Object[]) args);

        Options options = new Options();

        Option environment = new Option("e", "env", true, "Environment");
        options.addOption(environment);

        Option action = new Option("a", "action", true, "Action");
        options.addOption(action);

        Option service = new Option("s", "service", true, "Service");
        options.addOption(service);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            Command command = new Command();

            String envValue = cmd.getOptionValue("env");
            command.setEnvironment(envValue);

            String actionValue = cmd.getOptionValue("action");
            command.setAction(actionValue);

            String serviceValue = cmd.getOptionValue("service");
            command.setService(serviceValue);

            return command;
        } catch (ParseException e) {
            LOGGER.error("Exception while parsing command", e);
            formatter.printHelp("utility-name", options);
        }

        return null;
    }
}
