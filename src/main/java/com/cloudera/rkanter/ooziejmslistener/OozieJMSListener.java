package com.cloudera.rkanter.ooziejmslistener;

import java.util.Date;
import java.util.Properties;
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

    private Connection connection = null;

    public static void main(String[] args) throws Exception {
        if (args.length != 1 && args.length != 2) {
            printUsage();
            throw new IllegalAccessException("Invalid arguments");
        }
        String oozieUrl = args[0];
        String user = null;
        if (args.length == 2) {
            user = args[1];
        }
        OozieJMSListener listener = new OozieJMSListener();
        listener.connect(oozieUrl, user);
    }

    private void connect(String oozieUrl, String user) throws Exception {
        OozieClient oozie = new OozieClient(oozieUrl);

        JMSConnectionInfo jmsInfo = oozie.getJMSConnectionInfo();
        Properties jndiProps = jmsInfo.getJNDIProperties();
        System.out.println("JNDI Properties");
        System.out.println("===============");
        jndiProps.list(System.out);
        System.out.println();

        Context jndiContext = new InitialContext(jndiProps);
        String connectionFactoryName = (String) jndiContext.getEnvironment().get("connectionFactoryNames");
        ConnectionFactory connectionFactory = (ConnectionFactory)jndiContext.lookup(connectionFactoryName);
        connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        String topicPrefix = jmsInfo.getTopicPrefix();
        String topicPattern = jmsInfo.getTopicPattern(AppType.WORKFLOW_JOB);
        // Following code checks if the topic pattern is
        //'username', then the topic name is set to the actual user submitting the job
        String topicName = null;
        if (topicPattern.equals("${username}")) {
            if (user == null) {
                throw new IllegalArgumentException("JMS topic is '${username}' so the 'username' argument must be specified");
            }
            topicName = user;
        }
        Destination topic = session.createTopic(topicPrefix+topicName);
        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(this);
        connection.start();
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
        System.out.println("Listening on topic: " + topic.toString());
        System.out.println();
    }

    public void onMessage(Message message) {
        try {
            if (message.getStringProperty(JMSHeaderConstants.MESSAGE_TYPE).equals(MessageType.SLA.name())) {
                SLAMessage slaMessage = JMSMessagingUtils.getEventMessage(message);
                System.out.println("=== SLA Message ===");
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
        }
    }

    private static void printUsage() {
        System.out.println("Usage: OozieJMSListener <oozie-url> [username]");
        System.out.println();
    }
}
