/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;

/**
 * The step for settings the file state to 'missing' for all the files, which are currently set to unknown.
 */
public class SetOldUnknownFilesToMissingStep extends AbstractWorkFlowStep {
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    
    /**
     * Constructor.
     * @param store The storage for the integrity data.
     */
    public SetOldUnknownFilesToMissingStep(IntegrityModel store) {
        this.store = store;
    }
    
    @Override
    public String getName() {
        return "Detecting missing files";
    }

    @Override
    public synchronized void performStep() {
        super.performStep();
        store.setOldUnknownFilesToMissing();
    }

    @Override
    public String getDescription() {
        return "Finds all files which have been in state 'Unknown' on a pillar for so long that they should be marked" +
                " as missing";
    }
}
