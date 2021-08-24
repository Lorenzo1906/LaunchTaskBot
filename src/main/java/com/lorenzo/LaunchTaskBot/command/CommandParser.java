package com.lorenzo.LaunchTaskBot.command;

import org.apache.commons.cli.*;

public class CommandParser {
    public static Command parse(String[] args) {
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
            command.setEnvironmentLabel(envValue);

            String actionValue = cmd.getOptionValue("action");
            command.setAction(actionValue);
            command.setActionLabel(actionValue);

            String serviceValue = cmd.getOptionValue("service");
            command.setService(serviceValue);
            command.setServiceLabel(serviceValue);

            return command;
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
        }

        return null;
    }
}
