package com.github.rkanter.ooziejmslistener;

import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Semaphore;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.oozie.AppType;
import org.apache.oozie.client.JMSConnectionInfo;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.event.Event.MessageType;
import org.apache.oozie.client.event.jms.JMSHeaderConstants;
import org.apache.oozie.client.event.jms.JMSMessagingUtils;
import org.apache.oozie.client.event.message.JobMessage;
import org.apache.oozie.client.event.message.SLAMessage;

public class OozieJMSListener implements MessageListener {

    private String topic;
    private static Semaphore printSem = new Semaphore(1);

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 3 && args.length != 5) {
            printUsage();
            throw new IllegalAccessException("Invalid arguments");
        }
        String oozieUrl = args[0];
        String user = null;
        String jobId = null;
        if (args.length >= 3) {
            if (args[1].equals("-username")) {
                user = args[2];
            } else if (args[1].equals("-jobid")) {
                    jobId = args[2];
            }
            else {
                printUsage();
                throw new IllegalAccessException("Invalid arguments");
            }
        }
        if (args.length == 5) {
            if (args[3].equals("-username")) {
                user = args[4];
            } else if (args[3].equals("-jobid")) {
                    jobId = args[4];
            }
            else {
                printUsage();
                throw new IllegalAccessException("Invalid arguments");
            }
        }

        OozieClient oozie = new OozieClient(oozieUrl);
        JMSConnectionInfo jmsInfo = oozie.getJMSConnectionInfo();
        Properties jndiProps = jmsInfo.getJNDIProperties();
        System.out.println("JNDI Properties");
        System.out.println("===============");
        jndiProps.list(System.out);
        System.out.println();

        StringBuilder sbTopics = new StringBuilder();
        Set<String> topics = getTopics(jmsInfo, user, jobId);
        for (String t : topics) {
            sbTopics.append("\n\t").append(t);
        }
        System.out.println("Listening on topics: " + sbTopics.toString());
        System.out.println();
        for (String t : topics) {
            OozieJMSListener oListener = new OozieJMSListener(t);
            oListener.connect(jndiProps);
        }
    }

    private OozieJMSListener(String topic) {
        this.topic = topic;
    }

    private void connect(Properties jndiProps) throws Exception {
        Context jndiContext = new InitialContext(jndiProps);
        String connectionFactoryName = (String) jndiContext.getEnvironment().get("connectionFactoryNames");
        ConnectionFactory connectionFactory = (ConnectionFactory)jndiContext.lookup(connectionFactoryName);
        final Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination topicD = session.createTopic(topic);
        MessageConsumer consumer = session.createConsumer(topicD);
        consumer.setMessageListener(this);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private static Set<String> getTopics(JMSConnectionInfo jmsInfo, String user, String jobId) {
        Set<String> topics = new HashSet<String>();
        String topicPrefix = jmsInfo.getTopicPrefix();
        String topicPattern = jmsInfo.getTopicPattern(AppType.WORKFLOW_JOB);
        topics.add(getTopicsHelper(topicPrefix, topicPattern, user, jobId));
        topicPattern = jmsInfo.getTopicPattern(AppType.COORDINATOR_JOB);
        topics.add(getTopicsHelper(topicPrefix, topicPattern, user, jobId));
        topicPattern = jmsInfo.getTopicPattern(AppType.BUNDLE_JOB);
        topics.add(getTopicsHelper(topicPrefix, topicPattern, user, jobId));
        return topics;
    }

    private static String getTopicsHelper(String topicPrefix, String topicPattern, String user, String jobId) {
        String topicName = topicPattern;
        if (topicPattern.equals("${username}")) {
            if (user == null) {
                throw new IllegalArgumentException("JMS topic is '${username}' so the '-username' argument must be specified");
            }
            topicName = user;
        }
        if (topicPattern.equals("${jobId}")) {
            if (jobId == null) {
                throw new IllegalArgumentException("JMS topic is '${jobId}' so the '-jobId' argument must be specified");
            }
            topicName = jobId;
        }
        return topicPrefix + topicName;
    }

    public void onMessage(Message message) {
        try {
            printSem.acquire();
            if (message.getStringProperty(JMSHeaderConstants.MESSAGE_TYPE).equals(MessageType.SLA.name())) {
                SLAMessage slaMessage = JMSMessagingUtils.getEventMessage(message);
                System.out.println("=== SLA Message ===");
                System.out.println("JMSTopic            : " + topic);
                System.out.println("JMSTimestamp        : " + new Date(message.getJMSTimestamp()));
                System.out.println("JMSMessageID        : " + message.getJMSMessageID());
                System.out.println("appType             : " + slaMessage.getAppType());
                System.out.println("id                  : " + slaMessage.getId());
                System.out.println("parentId            : " + slaMessage.getParentId());
                System.out.println("nominalTime         : " + slaMessage.getNominalTime());
                System.out.println("expectedStartTime   : " + slaMessage.getExpectedStartTime());
                System.out.println("actualStartTime     : " + slaMessage.getActualStartTime());
                System.out.println("expectedEndTime     : " + slaMessage.getExpectedEndTime());
                System.out.println("actualEndTime       : " + slaMessage.getActualEndTime());
                System.out.println("expectedDuration    : " + slaMessage.getExpectedDuration());
                System.out.println("actualDuration      : " + slaMessage.getActualDuration());
                System.out.println("notificationMessage : " + slaMessage.getNotificationMessage());
                System.out.println("upstreamApps        : " + slaMessage.getUpstreamApps());
                System.out.println("user                : " + slaMessage.getUser());
                System.out.println("appName             : " + slaMessage.getAppName());
                System.out.println("eventStatus         : " + slaMessage.getEventStatus());
                System.out.println("slaStatus           : " + slaMessage.getSLAStatus());
                System.out.println();
            }
            else {
                JobMessage jobMessage = JMSMessagingUtils.getEventMessage(message);
                System.out.println("=== Job Message ===");
                System.out.println("JMSTopic            : " + topic);
                System.out.println("JMSTimestamp        : " + new Date(message.getJMSTimestamp()));
                System.out.println("JMSMessageID        : " + message.getJMSMessageID());
                System.out.println("appType             : " + jobMessage.getAppType());
                System.out.println("id                  : " + jobMessage.getId());
                System.out.println("parentId            : " + jobMessage.getParentId());
                System.out.println("startTime           : " + jobMessage.getStartTime());
                System.out.println("endTime             : " + jobMessage.getEndTime());
                System.out.println("eventStatus         : " + jobMessage.getEventStatus());
                System.out.println("appName             : " + jobMessage.getAppName());
                System.out.println("user                : " + jobMessage.getUser());
                System.out.println();
           }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            printSem.release();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: OozieJMSListener <oozie-url> [-username <username>] [-jobid <jobId>]");
        System.out.println();
    }
}
