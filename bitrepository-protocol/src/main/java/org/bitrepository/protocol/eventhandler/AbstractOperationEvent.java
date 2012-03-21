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
package org.bitrepository.protocol.eventhandler;

/** A abstract implementation of <code>OperationEvent</code> . 
 * @param <T>*/
public abstract class AbstractOperationEvent<T> implements OperationEvent<T> {
    /** @see #getType() */
    protected final OperationEventType type;
    /** @see #getInfo() */
    protected final String info;

    /** The constructor for this immutable */
    public AbstractOperationEvent(OperationEventType type, String info) {
        this.type = type;
        this.info = info;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public OperationEventType getType() {
        return type;
    }

    @Override 
    public final String toString() {
        return getType() + " event: " + "[ID: " + getID() + "] " + getInfo() + additionalInfo();
    }
    
    /**
     * Deliver additional information in a string form. 
     */
    public abstract String additionalInfo();
    
    /**
     * Deliver the correlation ID of the event. 
     */
    public abstract String getID();
}