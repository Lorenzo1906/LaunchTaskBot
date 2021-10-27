package com.lorenzo.LaunchTaskBot.command;

import com.lorenzo.LaunchTaskBot.data.model.Action;
import com.lorenzo.LaunchTaskBot.data.model.Audit;
import com.lorenzo.LaunchTaskBot.data.model.Project;
import com.lorenzo.LaunchTaskBot.data.repository.ActionRepository;
import com.lorenzo.LaunchTaskBot.data.repository.AuditRepository;
import com.lorenzo.LaunchTaskBot.data.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Thread.sleep;

@Component
public class CommandActions {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandActions.class);

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private static final int EIGHT_HOURS = 480 * 60 * 1000;
    private static final int THIRTY_SECONDS = 30 * 1000;

    public boolean executeCommand(Command command, String channelName, String user) {
        boolean result = true;
        List<Action> actions = actionRepository.findActionByParams(command.getAction(), command.getService(), command.getEnvironment(), channelName);
        Audit audit;

        if (actions.size() > 1) {
            LOGGER.error("More than one action returned for current configuration");


            audit = generateAudit(user, command.getAction(), command.getProject(), command.getEnvironment(), "More than one action returned for current configuration");
            result = false;
        } else if (actions.size() < 1) {
            LOGGER.error("No action returned for current configuration");

            audit = generateAudit(user, command.getAction(), command.getProject(), command.getEnvironment(), "No action returned for current configuration");
            result = false;
        } else {
            LOGGER.debug("Action found, executing action.");

            String credentials = System.getenv("BAMBOO_CREDENTIALS");
            if (credentials == null || credentials.isBlank()) {
                LOGGER.error("Bamboo credentials not found");

                audit = generateAudit(user, command.getAction(), command.getProject(), command.getEnvironment(), "Bamboo credentials not found");
                result = false;
            } else {
                LOGGER.debug("Bamboo credentials found");

                try {
                    Action action = actions.get(0);

                    URL url = new URL(action.getUrl());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", "Bearer " + credentials);

                    LOGGER.debug("Invoking URL: {}", action.getUrl());

                    if (conn.getResponseCode() != 200) {
                        LOGGER.error("Failed : HTTP error code : {}", conn.getResponseCode());

                        audit = generateAudit(user, command.getAction(), command.getProject(), command.getEnvironment(), "Failed : HTTP error code : " + conn.getResponseCode());
                        result = false;
                    } else {
                        String resultUrl = "";
                        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                        String output;
                        LOGGER.debug("Output from Server ....");
                        while ((output = br.readLine()) != null) {

                            resultUrl = getResultUrlFromResponse(output);
                        }

                        conn.disconnect();

                        result = getPlanResult(resultUrl, credentials);
                        audit = generateAudit(user, command.getAction(), command.getProject(), command.getEnvironment(), String.valueOf(result));
                    }
                } catch (IOException e) {
                    audit = generateAudit(user, command.getAction(), command.getProject(), command.getEnvironment(), e.getLocalizedMessage());
                    LOGGER.error(e.getLocalizedMessage(), e);
                }
            }
        }

        auditRepository.save(audit);

        return result;
    }

    private Audit generateAudit(String user, String action, String project, String environment, String state) {
        Audit audit = new Audit();

        audit.setUser(user);
        audit.setAction(action);
        audit.setProject(project);
        audit.setEnvironment(environment);
        audit.setState(state);
        audit.setCreated(new Date());

        return audit;
    }

    /**
     * The method is going to check the result of the execution, is going to call the service every 30 seconds, and it
     * will stop if the service returns an answer or more than 8 hours passed.
     * @param resultUrl The url to ba call to check the result of the execution
     * @param credentials The value of the environment variable BAMBOO_CREDENTIALS
     * @return true if the value was successful or false if was an error.
     */
    private boolean getPlanResult(String resultUrl, String credentials) {
        boolean result = true;

        long startTime = System.currentTimeMillis();

        boolean cont = true;
        while (cont) {
            long estimatedTime = System.currentTimeMillis() - startTime;
            if (estimatedTime > EIGHT_HOURS) {
                cont = false;
            }

            String value = getPlanResultStatus(resultUrl, credentials);

            switch (value) {
                case "Unknown":
                    try {
                        sleep(THIRTY_SECONDS);
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }
                    break;
                case "Successful":
                    cont = false;
                    break;
                case "error":
                    result = false;
                    cont = false;
                    break;
            }
        }

        return result;
    }

    private String getPlanResultStatus(String resultUrl, String credentials) {
        String result = "";

        try {
            URL url = new URL(resultUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + credentials);

            LOGGER.debug("Invoking URL: {}", resultUrl);

            if (conn.getResponseCode() != 200) {
                LOGGER.error("Failed : HTTP error code : {}", conn.getResponseCode());

                result = "error";
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                String output;
                LOGGER.debug("Output from Server ....");
                while ((output = br.readLine()) != null) {

                    result = getResultStatusFromResponse(output);
                }

                conn.disconnect();
            }
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        return result;
    }

    private String getResultStatusFromResponse(String response) {
        String result = "";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(response)));
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("result");

            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList links = element.getElementsByTagName("buildState");

                    for (int i = 0; i < links.getLength(); i++) {
                        Node linkNode = links.item(temp);
                        result = linkNode.getTextContent();
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        return result;
    }

    private String getResultUrlFromResponse(String response) {
        String result = "";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(response)));
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("restQueuedBuild");

            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList links = element.getElementsByTagName("link");

                    for (int i = 0; i < links.getLength(); i++) {
                        Node linkNode = links.item(temp);

                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element linkElement = (Element) linkNode;

                            result = linkElement.getAttribute("href");
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        return result;
    }

    public List<String> getEnvByProject(long projectId) {
        Set<String> values = new HashSet<>();
        List<Action> actions = actionRepository.findByProject_IdEquals(projectId);

        for (Action action: actions) {
            values.add(action.getEnvironment());
        }

        return List.copyOf(values);
    }

    public List<String> getServicesByProjectAndEnvironment(long projectId, String env) {
        Set<String> values = new HashSet<>();
        List<Action> actions = actionRepository.findByProject_IdEqualsAndEnvironmentLikeIgnoreCase(projectId, env);

        for (Action action: actions) {
            values.add(action.getService());
        }

        return List.copyOf(values);
    }

    public List<String> getActionsByProjectAndEnvironmentAndService(long projectId, String env, String service) {
        Set<String> values = new HashSet<>();
        List<Action> actions = actionRepository.findByProject_IdEqualsAndEnvironmentLikeIgnoreCaseAndServiceLikeIgnoreCase(projectId, env, service);

        for (Action action: actions) {
            values.add(action.getName());
        }

        return List.copyOf(values);
    }

    public Project getProjectByChannelName(String channelName) {
        return projectRepository.findBySlackChannelIgnoreCase(channelName);
    }

    public Command getCommandInfo(Command command, String channelName) {
        LOGGER.info("Getting command info for channel {}", channelName);

        List<Action> actions = actionRepository.findActionByParams(command.getAction(), command.getService(), command.getEnvironment(), channelName);

        if (actions.size() != 1){
            LOGGER.debug("Not command info found for channel {}", channelName);
            return null;
        }

        Action action = actions.get(0);

        command.setEnvironment(action.getEnvironment());
        command.setAction(action.getName());
        command.setService(action.getService());
        command.setUrl(action.getUrl());
        command.setProject(action.getProject().getName());

        LOGGER.debug("Command info found for channel {}", channelName);

        return command;
    }
}
