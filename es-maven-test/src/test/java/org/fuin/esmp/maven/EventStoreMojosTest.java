/**
 * Copyright (C) 2015 Michael Schnell. All rights reserved. 
 * http://www.fuin.org/
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
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.esmp.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link EventStoreMojo}.
 */
public class EventStoreMojosTest {

    // CHECKSTYLE:OFF Test

    @Test
    public void testMojo() throws VerificationException, IOException {

        // PREPARE
        final Verifier verifier = new Verifier(new File("target/test-classes/test-project").getAbsolutePath(), true);
        try {
            verifier.deleteArtifacts("org.fuin.esmp", "esmp-test-project", "0.0.1");
            
            final List<String> goals = new ArrayList<String>();
            goals.add("clean");
            goals.add("verify");
    
            // TEST
            verifier.executeGoals(goals);
    
            // VERIFY
            System.out.println(
                    "=================================== PLUGIN OUTPUT BEGIN ===================================");
            final List<String> lines = verifier.loadFile(verifier.getBasedir(), verifier.getLogFileName(), false);
            for (final String line : lines) {
                System.out.println(line);
            }
            System.out.println(
                    "=================================== PLUGIN OUTPUT END =====================================");
            verifier.verifyErrorFreeLog();
    
            // download
            verifier.verifyTextInLog("Dowloading archive:");
            verifier.verifyTextInLog("Archive copied from ");
            verifier.verifyTextInLog("Unpack event store to target directory:");
    
            // certificate
            verifier.verifyTextInLog("Certificate successfully created");
            
            // start
            verifier.verifyTextInLog("Event store process ID:");
    
            // stop
            verifier.verifyTextInLog("Event store successfully stopped");
            
        } finally {
            verifier.displayStreamBuffers();
        }

    }

    @Test
    public void testMojoConfig() throws VerificationException, IOException {

        // PREPARE
        final Verifier verifier = new Verifier(new File("target/test-classes/test-project-2").getAbsolutePath(), true);
        try {
            verifier.deleteArtifacts("org.fuin.esmp", "esmp-test-project-2", "0.0.1");
            
            final List<String> goals = new ArrayList<String>();
            goals.add("clean");
            goals.add("verify");
    
            // TEST
            verifier.executeGoals(goals);
    
            // VERIFY
            System.out.println(
                    "=================================== PLUGIN OUTPUT BEGIN ===================================");
            final List<String> lines = verifier.loadFile(verifier.getBasedir(), verifier.getLogFileName(), false);
            for (final String line : lines) {
                System.out.println(line);
            }
            System.out.println(
                    "=================================== PLUGIN OUTPUT END =====================================");
            verifier.verifyErrorFreeLog();
    
            // download
            verifier.verifyTextInLog("Dowloading archive:");
            verifier.verifyTextInLog("Archive copied from ");
            verifier.verifyTextInLog("Unpack event store to target directory:");
    
            // certificate
            verifier.verifyTextInLog("Certificate successfully created");
            
            // start
            verifier.verifyTextInLog("Event store process ID:");
    
            // stop
            verifier.verifyTextInLog("Event store successfully stopped");
            
        } finally {
            verifier.displayStreamBuffers();
        }

    }
    
    // CHECKSTYLE:OFF Test

}
