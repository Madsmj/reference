package org.bitrepository.audittrails.preserver;

import java.util.Map;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The event handler for the preservation of audit trail data.
 * When the PutFile operation has completed, then the store will be updated with the results.
 * 
 * It is not necessary to wait until all the components are complete. Just the first.
 */
public class AuditPreservationEventHandler implements EventHandler {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The map between the contributors and their sequence number.*/
    private final Map<String, Long> seqNumbers;
    /** The store for the audit trails. Where the new preservation sequence numbers should be inserted.*/
    private final AuditTrailStore store;
    /** Whether the store has been updated with the values.*/
    private boolean updated;
    
    /**
     * Constructor.
     * @param preservationSequenceNumber The map between the contributor ids and their respective sequence number.
     * @param store The store which should be updated with these sequence numbers.
     */
    public AuditPreservationEventHandler(Map<String, Long> preservationSequenceNumber, AuditTrailStore store) {
        this.seqNumbers = preservationSequenceNumber;
        this.store = store;
        this.updated = false;
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        if(event.getType() == OperationEventType.COMPLETE
                || event.getType() == OperationEventType.COMPONENT_COMPLETE) {
            updateStoreWithResults();
        } else {
            log.debug("Event for preservation of audit trails: " + event.toString());
        }
    }
    
    /**
     * Update the store with the results.
     */
    private void updateStoreWithResults() {
        if(updated) {
            log.debug("Have already updated the store with the new preservation sequence number.");
            return;
        }
        updated = true;
        
        for(Map.Entry<String, Long> entry : seqNumbers.entrySet()) {
            store.setPreservationSequenceNumber(entry.getKey(), entry.getValue());
        }
    }
}
