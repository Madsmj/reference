/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.conversation;

import org.bitrepository.protocol.exceptions.ConversationTimedOutException;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageListener;

/**
 * A conversation models the messaging based workflow used to accomplish a operation called on a client interface.
 * 
 * The interface primarily consists of sending and receiving message functionality.
 *
 * @param <T> The outcome of the conversation.
 */
public interface Conversation<T> extends MessageListener {
    /**
     * Set the mediator that handles this conversation. This method should always be called by the mediator
     * that manages the conversation, immediately after the conversation is registered.
     *
     * Implementations of conversations may use this mediator to end the conversation from within.
     *
     * @param mediator The handler that handles this conversation.
     */
    void setMediator(ConversationMediator mediator);

    /**
     * Get the conversation ID for this conversation.
     *
     * Implementations must ensure that conversation IDs are unique, and that this ID is always used when sending
     * messages.
     *
     * @return The conversation ID.
     */
    String getConversationID();

    /**
     * Return whether this conversation has ended.
     * Once this returns true it should never return false again. Conversations cannot be reused.
     *
     * @return Whether this conversation has ended.
     */
    boolean hasEnded();

    /**
     * Get the result of this conversation. This will be null, until the conversation has ended,
     * and may be null after the conversation has ended, in case no result can be generated.
     *
     * @return The result of the conversation.
     */
    T getResult();

    /**
     * Block until the conversation is finished.
     *
     * @return The result of the conversation.
     */
    T waitFor();

    /**
     * Block until the conversation is finished, or until timeout has occurred.
     *
     * @param timeout Timeout (in milliseconds)
     *
     * @return The result of the conversation.
     *
     * @throws ConversationTimedOutException On timeout. A partial result MAY be available through
     * {@link #getResult()}
     *
     */
    T waitFor(long timeout) throws ConversationTimedOutException;

    /**
     * @throws ConversationTimedOutException 
     * 
     */
    void startConversion() throws OperationFailedException;
}