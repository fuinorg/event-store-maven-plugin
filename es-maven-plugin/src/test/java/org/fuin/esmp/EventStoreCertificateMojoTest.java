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
package org.fuin.esmp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

/**
 * Test for {@link EventStoreCertificateMojo}.
 */
public class EventStoreCertificateMojoTest {

    // CHECKSTYLE:OFF Test

    @Test
    public void testExecute() throws MojoExecutionException, IOException {

        // PREPARE
        final EventStoreCertificateMojo testee = new EventStoreCertificateMojo();
        final File p12File = File.createTempFile("EventStoreCertificateMojoTest-", ".p12");
        testee.setCertificateFile(p12File.toString());

        // TEST
        testee.execute();

        // VERIFY
        assertThat(p12File).exists();
        assertThat(p12File.length()).isGreaterThan(0);

    }

    // CHECKSTYLE:ON

}
