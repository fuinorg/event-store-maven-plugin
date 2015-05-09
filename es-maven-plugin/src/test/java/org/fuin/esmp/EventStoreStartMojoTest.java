/**
 * Copyright (C) 2015 Michael Schnell. All rights reserved. 
 * <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fuin.esmp;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link EventStoreStartMojo}.
 */
public class EventStoreStartMojoTest {

    // CHECKSTYLE:OFF Test
    
    @Before
    public void setup() throws MojoExecutionException {
        final EventStoreDownloadMojo testee = new EventStoreDownloadMojo();
        testee.execute();
    }
    
    @After
    public void tearDown() throws MojoExecutionException {
        final EventStoreStopMojo testee = new EventStoreStopMojo();
        testee.execute();
    }
    
    @Test
    public void testExecute() throws MojoExecutionException {
        
        // PREPARE
        final EventStoreStartMojo testee = new EventStoreStartMojo();
        
        // TEST
        testee.execute();        
        
        // VERIFY        
        assertThat(true).isTrue();
        
    }
    
    // CHECKSTYLE:ON
    
}
