/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.clic;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.network.jms.JmsMesageConvertor;
import org.apache.log4j.Logger;

import cern.dmn2.agentlib.AgentMessage;
import cern.dmn2.agentlib.AgentMessageException;
import cern.dmn2.agentlib.FieldDataType;
import cern.dmn2.agentlib.MessageHeader;
import cern.dmn2.agentlib.impl.AgentMessageImpl;

/**
 * @author wbuczak
 */
public class DummyClicAgent {

    
    private static final Logger log = Logger.getLogger(DummyClicAgent.class);

    private final String brokerUrl;
    
    private final String agentDevice;
    private final long heartbeatLoopTime;
    private final long acqusitionLoopTime;

    public DummyClicAgent(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        this.agentDevice = "DMN.CLIC.TEST";
        this.heartbeatLoopTime = 1000;
        this.acqusitionLoopTime = 1500;
    }

    public DummyClicAgent(String brokerUrl, String agentDeviceName, long heartbeatLoopTime, long acquisitionLoopTime) {
        this.brokerUrl = brokerUrl;
        this.agentDevice = agentDeviceName;
        this.heartbeatLoopTime = heartbeatLoopTime;
        this.acqusitionLoopTime = acquisitionLoopTime;
    }

    ActiveMQConnectionFactory factory;
    Connection conn;
    Session session;

    MessageProducer prod;
    MessageConsumer cmdMessageConsumer;

    int heartbeatCounter = 1;

    Thread hbSenderThread;
    Thread aqSenderThread;

    private volatile boolean runHeartbeatThread = true;
    private volatile boolean runAcquisitionThread = true;

    Session hbSession;

    class HeartbeatSender implements Runnable {

        @Override
        public void run() {
            try {
                hbSession = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic hbTopic = session.createTopic("CERN.DIAMON." + agentDevice + ".Heartbeat");

                while (runHeartbeatThread) {
                    sendHeartbeat(hbTopic);
                    Thread.sleep(heartbeatLoopTime);
                }

                hbSession.close();
            }

            catch (Exception e) {
            } finally {
                if (hbSession != null)
                    try {
                        hbSession.close();
                    } catch (Exception ex) {
                    }
            }
        }

    }

    class AcquisitionSender implements Runnable {

        @Override
        public void run() {
            try {
                hbSession = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic acqTopic = session.createTopic("CERN.DIAMON." + agentDevice + ".Acquisition");

                while (runAcquisitionThread) {
                    log.debug("sending acquisition");
                    sendAcquisition(acqTopic);
                    Thread.sleep(acqusitionLoopTime);
                }

                hbSession.close();
            }

            catch (Exception e) {
            } finally {
                if (hbSession != null)
                    try {
                        hbSession.close();
                    } catch (Exception ex) {
                    }
            }
        }

    }

    public synchronized void startHeartbeat() {
        hbSenderThread = new Thread(new HeartbeatSender());
        hbSenderThread.start();
    }

    public synchronized void stopHeartbeat() {
        runHeartbeatThread = false;
    }

    public synchronized void startAcquisition() {
        aqSenderThread = new Thread(new AcquisitionSender());
        aqSenderThread.start();
    }

    public synchronized void stopAcquisition() {
        runAcquisitionThread = false;
    }

    private void sendHeartbeat(Destination jmsDestination) throws JMSException {
        MessageProducer producer = session.createProducer(jmsDestination);
        TextMessage message = session.createTextMessage();

        // set header properties
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_AGENT_DEVICE_NAME, agentDevice);
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_PROPERTY_NAME, "Heartbeat");
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_AGENT_CLASS, "CLIC");

        // set msg. body
        StringBuilder strbld = new StringBuilder();
        strbld.append(FieldDataType.TYPE_LONG.getValueString() + "/1/dmn.agent.loops=2\n");
        strbld.append(FieldDataType.TYPE_STRING.getValueString()
                + "/24/dmn.agent.loops.details=loopCounter of ClicSleep\n");
        strbld.append(FieldDataType.TYPE_LONG.getValueString() + "/13/dmn.agent.loops.ts=" + System.currentTimeMillis()
                + "\n");
        strbld.append(FieldDataType.TYPE_LONG.getValueString() + "/1/heartbeatCounter=" + (heartbeatCounter++) + "\n");
        message.setText(strbld.toString());

        // send the message
        producer.send(message);
        producer.close();
    }

    private void sendAcquisition(Destination jmsDestination) throws JMSException {
        MessageProducer producer = session.createProducer(jmsDestination);
        TextMessage message = session.createTextMessage();

        // set header properties
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_AGENT_DEVICE_NAME, agentDevice);
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_PROPERTY_NAME, "Acquisition");
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_AGENT_CLASS, "CLIC");

        // set msg. body
        StringBuilder strbld = new StringBuilder();
        strbld.append(FieldDataType.TYPE_LONG.getValueString() + "/1/test.property1=1\n");
        strbld.append(FieldDataType.TYPE_LONG.getValueString() + "/1/test.property2=2\n");
        strbld.append(FieldDataType.TYPE_LONG.getValueString() + "/1/test.property3=3\n");

        message.setText(strbld.toString());

        log.debug("Sending Acquisition");
        // send the message
        producer.send(message);
        producer.close();
    }

    private void handleRestartProcessRequest(Message requestMessage) throws JMSException {

        Destination replyToDest = requestMessage.getJMSReplyTo();

        AgentMessage am = null;
        try {
            am = new AgentMessageImpl((TextMessage) requestMessage);
        } catch (AgentMessageException e) {
            log.error(e);
            return;
        }

        String resultString = "";

        if (am.getBody().containsKey("process")) {
            String process = (String) am.getBody().get("process");
            resultString = "process " + process + " restarted";
        }

        MessageProducer producer = session.createProducer(replyToDest);
        TextMessage message = session.createTextMessage();

        // set header properties
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_AGENT_DEVICE_NAME, agentDevice);
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_PROPERTY_NAME, "Command");
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_AGENT_CLASS, "CLIC");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        // set msg. body
        
        StringBuilder responseBld = new StringBuilder();
        responseBld.append(FieldDataType.TYPE_STRING.getValueString() + "/"+resultString.length()+"/response=").append(resultString).append("\n\n");
        message.setText(responseBld.toString());

        log.debug("Sending command reply");
        // send the message
        producer.send(message);
        producer.close();

    }
    
    private void handleCommandRequest(Message requestMessage) throws JMSException {

        Destination replyToDest = requestMessage.getJMSReplyTo();

        AgentMessage am = null;
        try {
            am = new AgentMessageImpl((TextMessage) requestMessage);
        } catch (AgentMessageException e) {
            log.error(e);
            return;
        }

        

        String commandName = null;
        String args = null;
        
        if (am.getBody().containsKey("command")) {
            commandName = (String) am.getBody().get("command");            
        }

        if (am.getBody().containsKey("args")) {
            args = (String) am.getBody().get("args");            
        }
                
        MessageProducer producer = session.createProducer(replyToDest);
        TextMessage message = session.createTextMessage();

        // set header properties
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_AGENT_DEVICE_NAME, agentDevice);
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_PROPERTY_NAME, "Command");
        message.setStringProperty(MessageHeader.HEADER_PROPERTY_AGENT_CLASS, "CLIC");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
                
        String resultString = String.format("received command: %s with arguments: %s",commandName,args);
        log.debug("result="+resultString);
        // set msg. body
        
        StringBuilder responseBld = new StringBuilder();
        responseBld.append(FieldDataType.TYPE_STRING.getValueString() + "/"+resultString.length()+"/response=").append(resultString).append("\n\n");
        message.setText(responseBld.toString());

        log.debug("Sending command reply");
        // send the message
        producer.send(message);
        producer.close();

    }

    
    
    
    public synchronized void start() throws Exception {

        factory = new ActiveMQConnectionFactory("", "", this.brokerUrl);
        factory.setWatchTopicAdvisories(false);

        conn = factory.createConnection();

        conn.setClientID("AGENT-CONN");
        conn.start();

        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic commandtopic = session.createTopic("CERN.DIAMON." + agentDevice + ".commands");

        cmdMessageConsumer = session.createConsumer(commandtopic);

        MessageListener listener = new MessageListener() {

            @Override
            public void onMessage(Message message) {
                try {
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;

                        try {
                            AgentMessage am = new AgentMessageImpl(textMessage);

                            switch (am.getHeader().getCommandType()) {
                            case SET:
                                log.debug("set comand received");
                                break;
                            case GET:
                                log.debug("get comand received");

                                String agentProperty = am.getHeader().getAgentProperty();

                                if (agentProperty.equals("Acquisition")) {
                                    // reply with acquisition, as requested
                                    sendAcquisition(message.getJMSReplyTo());
                                } else if (agentProperty.equals("Heartbeat")) {
                                    // reply with heartbeat, as requested
                                    sendHeartbeat(message.getJMSReplyTo());
                                } else if (agentProperty.equals("RestartProcess")) {
                                    handleRestartProcessRequest(message);
                                } else if (agentProperty.equals("Command")) {
                                    handleCommandRequest(message);                                
                                } else {
                                    log.warn("unknown comand received");
                                }
                            }
                        } catch (AgentMessageException e) {
                            log.warn("could not convert received message to AgentMessage", e);
                        }
                    }
                } catch (JMSException e) {
                    System.out.println("Caught:" + e);
                    e.printStackTrace();
                }
            }

        };

        cmdMessageConsumer.setMessageListener(listener);
    }

    public void stop() throws Exception {
        if (cmdMessageConsumer != null)
            cmdMessageConsumer.close();

        if (session != null) {
            session.close();
        }
        if (conn != null)
            conn.close();
    }

}