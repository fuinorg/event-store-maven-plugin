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
package org.fuin.srcgen4j.maven;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link EventStoreMojo}.
 */
public class EventStoreStartMojoTest {

    // CHECKSTYLE:OFF Test

    private static final File TEST_DIR = new File("target/test-classes/test-project");

    private Verifier verifier;

    @Before
    public void setup() throws Exception {
        verifier = new Verifier(TEST_DIR.getAbsolutePath());
        verifier.deleteArtifacts("org.fuin.esmp", "esmp-test-project", "0.0.1");
    }

    @Test
    public void testMojo() throws VerificationException {

        // PREPARE

        // TEST
        verifier.executeGoal("org.fuin.esmp:esmp-plugin:start");

        // VERIFY
        verifier.verifyErrorFreeLog();
        assertThat(true).isTrue();

    }

    // CHECKSTYLE:OFF Test

}