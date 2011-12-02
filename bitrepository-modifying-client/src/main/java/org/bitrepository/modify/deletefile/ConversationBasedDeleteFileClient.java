package org.bitrepository.modify.deletefile;

import javax.jms.JMSException;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.deletefile.conversation.SimpleDeleteFileConversation;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversationBasedDeleteFileClient implements DeleteFileClient {
    /** The log for this class.*/
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /** The mediator for the conversations for the PutFileClient.*/
    private final ConversationMediator conversationMediator;
    /** The message bus for communication.*/
    private final MessageBus bus;
    /** The settings. */
    private Settings settings;

    /**
     * Constructor.
     * @param messageBus The messagebus for communication.
     * @param settings The configurations and settings.
     */
    public ConversationBasedDeleteFileClient(MessageBus messageBus, ConversationMediator conversationMediator, Settings settings) {
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(settings, "settings");
        this.conversationMediator = conversationMediator;;
        this.bus = messageBus;
        this.settings = settings;
    }
    
    @Override
    public void deleteFile(String fileId, String pillarId, String checksum, ChecksumSpecTYPE checksumType,
            EventHandler eventHandler, String auditTrailInformation) 
                    throws OperationFailedException {
        ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(checksum, "String checksum");
        
        log.info("Requesting the deletion of the file '" + fileId + "' from the pillar '"
                + pillarId + "' with the checksum '" + checksum + "' and checksum specifications '" + checksumType 
                + "'. And the audit trail information '" + auditTrailInformation + "'.");
        SimpleDeleteFileConversation conversation = new SimpleDeleteFileConversation(bus, settings, fileId, pillarId, 
                checksum, checksumType, eventHandler, new FlowController(settings, false), auditTrailInformation);
        conversationMediator.addConversation(conversation);
        conversation.startConversation();
    }
    
    @Override
    public void shutdown() {
        try {
            bus.close();
            // TODO Kill any lingering timer threads
        } catch (JMSException e) {
            log.info("Error during shutdown of messagebus ", e);
        }
    }
    
}
