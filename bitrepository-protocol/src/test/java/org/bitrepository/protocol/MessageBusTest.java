/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.protocol;

import java.util.Date;

import org.apache.activemq.broker.BrokerService;
import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.collection.settings.standardsettings.MessageBusConfiguration;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.bitrepositorycollection.MutableCollectionSettings;
import org.bitrepository.protocol.bus.MessageBusConfigurationFactory;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.settings.CollectionSettingsLoader;
import org.bitrepository.protocol.settings.XMLFileSettingsLoader;
import org.custommonkey.xmlunit.XMLAssert;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Class for testing the interface with the message bus.
 */
public class MessageBusTest extends ExtendedTestCase {
    /** The time to wait when sending a message before it definitely should 
     * have been consumed by a listener.*/
    static final int TIME_FOR_MESSAGE_TRANSFER_WAIT = 500;
    
    /** The settings to use for this test */
    private MutableCollectionSettings settings;
    
    /**
     * Defines the standard BitRepositoryCollection configuration
     * @param testMethod Used to grap the name of the test method used for naming.
     * @throws Exception Something fishy happened. 
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeMethodSetup() throws Exception {
        setupSettings();
    }
    
    protected void setupSettings() throws Exception {
        CollectionSettingsLoader settingsLoader = new CollectionSettingsLoader(new XMLFileSettingsLoader("settings/xml"));
        settings = settingsLoader.loadSettings("bitrepository-devel");
    }

    @Test(groups = { "regressiontest" })
    public final void messageBusConnectionTest() {
        addDescription("Verifies that we are able to connect to the message bus");
        addStep("Get a connection to the message bus from the "
                + "<i>MessageBusConnection</i> connection class",
        "No exceptions should be thrown");
        Assert.assertNotNull(ProtocolComponentFactory.getInstance().getMessageBus(settings));
    }

    @Test(groups = { "regressiontest" })
    public final void busActivityTest() throws Exception {
        addDescription("Tests whether it is possible to create a message listener," +
                "and then set it to listen to the topic. Then puts a message" +
                "on the topic for the message listener to find, and" +
        "tests whether it finds the correct message.");

        IdentifyPillarsForGetFileRequest content = 
            ExampleMessageFactory.createMessage(IdentifyPillarsForGetFileRequest.class);
        TestMessageListener listener = new TestMessageListener();
        MessageBus con = ProtocolComponentFactory.getInstance().getMessageBus(settings);
        Assert.assertNotNull(con);
        con.addListener("BusActivityTest", listener);
        content.setTo("BusActivityTest");
        con.sendMessage(content);

        synchronized(this) {
            try {
                wait(TIME_FOR_MESSAGE_TRANSFER_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Assert.assertNotNull(listener.getMessage());
        XMLAssert.assertXMLEqual(JaxbHelper.serializeToXml(content),
                JaxbHelper.serializeToXml(listener.getMessage()));
    }

    @Test(groups = {"test-first"})
    public final void twoListenersForTopic() throws Exception {
        addDescription("Verifies that two listeners on the same topic both receive the message");

        //Test data
        String topicname = "BusActivityTest";
        IdentifyPillarsForGetFileRequest content = 
            ExampleMessageFactory.createMessage(IdentifyPillarsForGetFileRequest.class);

        addStep("Make a connection to the message bus and add two listeners", "No exceptions should be thrown");
        MessageBus con = ProtocolComponentFactory.getInstance().getMessageBus(settings);
        Assert.assertNotNull(con);

        TestMessageListener listener1 = new TestMessageListener();
        TestMessageListener listener2 = new TestMessageListener();
        con.addListener(topicname, listener1);
        con.addListener(topicname, listener2);

        addStep("Send a message to the topic", "No exceptions should be thrown");
        con.sendMessage(content);
        synchronized (this) {
            try {
                wait(5 * TIME_FOR_MESSAGE_TRANSFER_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        addStep("Make sure both listeners received the message",
                "Both listeners received the message, and it is identical");

        Assert.assertNotNull(listener1.getMessage());
        XMLAssert.assertXMLEqual(JaxbHelper.serializeToXml(content), JaxbHelper.serializeToXml(listener1.getMessage()));
        Assert.assertNotNull(listener2.getMessage());
        XMLAssert.assertXMLEqual(JaxbHelper.serializeToXml(content), JaxbHelper.serializeToXml(listener2.getMessage()));

    }

    @Test(groups = { "specificationonly" })
    public final void twoMessageBusConnectionTest() throws Exception {
        addDescription("Verifies that we are switch to a second message bus. "
                + "Awaiting introduction of robustness issue");
        addStep("Defining constants for this test.", "Should be allowed.");
        String QUEUE = "DUAL-MESSAGEBUS-TEST-" + new Date().getTime();

        addStep("Making the configurations for a embedded message bus.", "Should be allowed.");
        MessageBusConfiguration embeddedMBConfig = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();

        addStep("Start a embedded activeMQ instance based on the configuration.", "Should be allowed.");
        LocalActiveMQBroker broker = new LocalActiveMQBroker(embeddedMBConfig);
        try {
            broker.start();

            addStep("Making the configurations for the first message bus.", "Should be allowed.");
            MessageBusConfiguration config = new MessageBusConfiguration();
            config.setURL("tcp://sandkasse-01.kb.dk:61616");
            config.setName("kb-test-messagebus");
            config.setLogin("");
            config.setPassword("");

            addStep("Initiating the connection to the messagebus based on the first configuration", 
            "This should definitly be allowed.");
            MessageBus bus1 = new ActiveMQMessageBus(config);

            addStep("Initiating the connection to the messagebus based on the second configuration", 
            "It should be possible to have several message busses at the same time.");
            MessageBus bus2 = new ActiveMQMessageBus(embeddedMBConfig);

            addStep("Creating a test message to send.", "The interface is tested elsewhere and should work.");
            Alarm message1 = ExampleMessageFactory.createMessage(Alarm.class);
            Assert.assertNotNull(message1);
            message1.setTo(QUEUE);
            message1.setCorrelationID("1");

            addStep("Create and add a message listener to the first message bus.", "Should be allowed.");
            TestMessageListener listener1 = new TestMessageListener();
            Assert.assertNull(listener1.getMessage());
            bus1.addListener(QUEUE, listener1);

            addStep("Create and add a message listener to the second message bus.", "Should be allowed.");
            TestMessageListener listener2 = new TestMessageListener();
            Assert.assertNull(listener2.getMessage());
            bus2.addListener(QUEUE, listener2);

            addStep("Send the test message on messagebus 1.", "Should be received by listener 1.");
            bus1.sendMessage(message1);

            addStep("Wait for the message to be sent over the messagebus", "We wait.");
            synchronized (this) {
                try {
                    wait(TIME_FOR_MESSAGE_TRANSFER_WAIT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            addStep("Verify that the message is received by the message listener", 
            "It should be the same message as was sent.");
            Assert.assertNotNull(listener1.getMessage(), "The first message listener should have received a message.");
            Assert.assertEquals(listener1.getMessage().getClass(), message1.getClass());

            Assert.assertNull(listener2.getMessage(), "The second message listener should not have received a message.");

            addStep("Create a new message and send it over the other message bus.", "Should be allowed.");
            Alarm message2 = ExampleMessageFactory.createMessage(Alarm.class);
            message2.setTo(QUEUE);
            message2.setCorrelationID("2");
            bus2.sendMessage(message2);

            addStep("Wait for the message to be sent over the messagebus", "We wait.");
            synchronized (this) {
                try {
                    wait(10 * TIME_FOR_MESSAGE_TRANSFER_WAIT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            addStep("Verify that the message is received by the message listener", 
            "It should be the same message as was sent.");
            Assert.assertNotNull(listener2.getMessage(), "The second message listener should have received a message.");
            Assert.assertEquals(listener2.getMessage().getClass(), message2.getClass());

            Assert.assertNotNull(listener1.getMessage(), "The first message listener should have received a message.");
            Assert.assertEquals(listener1.getMessage().getClass(), message1.getClass());

            Assert.assertEquals(((Alarm) listener1.getMessage()).getCorrelationID(), "1");
            Assert.assertEquals(((Alarm) listener2.getMessage()).getCorrelationID(), "2");

        } finally {
            broker.stop();
        }
    }

    @Test(groups = { "specificationonly" })
    public final void messageBusFailoverTest() {
        addDescription("Verifies that we can switch to at second message bus " +
                "in the middle of a conversation, if the connection is lost. " +
                "We should also be able to resume the conversation on the new " +
        "message bus");
    }

    @Test(groups = { "specificationonly" })
    public final void messageBusReconnectTest() {
        addDescription("Test whether we are able to reconnect to the message " +
        "bus if the connection is lost");
    }

    /**
     * Temporary reverting to test-first, because of difficulty in changing messagebus at run time. This perhaps
     * reflects that
     * @throws Exception
     */
    @Test(groups = {"test-first", "connectiontest"})
    public final void localBrokerTest() throws Exception {
        addDescription("Tests the possibility for starting the broker locally,"
                + " and using it for communication by sending a simple message"
                + " over it and verifying that the corresponding message is "
                + "received.");
        IdentifyPillarsForGetFileRequest content = 
            ExampleMessageFactory.createMessage(IdentifyPillarsForGetFileRequest.class);

        addStep("Starting the local broker.", "A lot of info-level logs should"
                + " be seen here.");
        BrokerService broker = new BrokerService();
        broker.addConnector("tcp://localhost:61616");
        broker.start();

        synchronized(this) {
            try {
                this.wait(TIME_FOR_MESSAGE_TRANSFER_WAIT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            addStep("Connecting to the bus, and then connect to the local bus.", 
                    "Info-level logs should be seen here for both connections. "
                    + "Only the last is used.");
            MessageBus con = ProtocolComponentFactory.getInstance().getMessageBus(settings);
            //con = ConnectionFactory.getNextConnection();

            addStep("Make a listener for the messagebus and make it listen. "
                    + "Then send a message for the message listener to catch.",
            "several DEBUG-level logs");
            TestMessageListener listener = new TestMessageListener();
            con.addListener("EmbeddedBrokerTopic", listener);
            con.sendMessage(content);

            synchronized(this) {
                try {
                    this.wait(5 * TIME_FOR_MESSAGE_TRANSFER_WAIT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Assert.assertNotNull(listener.getMessage(), "A message should be received.");
            XMLAssert.assertEquals(JaxbHelper.serializeToXml(content),
                    JaxbHelper.serializeToXml(listener.getMessage()));

            con.removeListener("EmbeddedBrokerTopic", listener);
        } finally {
            broker.stop();
        }
    }

    protected class TestMessageListener extends AbstractMessageListener {
        /** Container for a message, when it is received.*/
        private Object message = null;
        @Override
        public final void onMessage(IdentifyPillarsForGetFileRequest message) {
            try {
                this.message = message;
            } catch (Exception e) {
                Assert.fail("Should not throw an exception: ", e);
            }
        }
        @Override
        public final void onMessage(Alarm message) {
            this.message = message;
        }

        /**
         * Retrieving the last message caught by this listener.
         * @return The last received message.
         */
        public final Object getMessage() {
            return message;
        }
    }
}
