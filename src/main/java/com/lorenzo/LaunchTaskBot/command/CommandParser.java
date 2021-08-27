package com.lorenzo.LaunchTaskBot.command;

import org.apache.commons.cli.*;
import org.springframework.stereotype.Component;

@Component
public class CommandParser {

    public Command parse(String[] args) {
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
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
        }

        return null;
    }
}
