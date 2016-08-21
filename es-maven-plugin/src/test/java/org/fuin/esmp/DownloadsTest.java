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
import java.util.UUID;

import org.fuin.utils4j.Utils4J;
import org.junit.Test;

/**
 * Test for {@link Downloads}.
 */
// CHECKSTYLE:OFF Test
public class DownloadsTest {

    @Test
    public void testDownload() throws IOException {

        // PREPARE
        final Downloads testee = new Downloads(
                new File(Utils4J.getTempDir(), "event-store-versions-" + UUID.randomUUID() + ".json"));

        // TEST
        testee.parse();

        // VERIFY
        assertThat(testee.getOsList()).containsExactlyInAnyOrder(new DownloadOS("ubuntu-14.04"),
                new DownloadOS("osx-10.10"), new DownloadOS("win"));
        final DownloadOS ubuntu = testee.findOS("ubuntu-14.04");
        assertThat(ubuntu.getOS()).isEqualTo("ubuntu-14.04");
        assertThat(ubuntu.getCurrentVersion()).isNotNull();

    }

    @Test
    public void testLocal() throws IOException {

        // PREPARE
        final Downloads testee = new Downloads(new File("./target/test-classes/test-download.json"));

        // TEST
        testee.parse();

        // VERIFY
        assertThat(testee.getOsList()).containsExactlyInAnyOrder(new DownloadOS("ubuntu-14.04"),
                new DownloadOS("osx-10.10"), new DownloadOS("win"));
        final DownloadOS ubuntu = testee.findOS("ubuntu-14.04");
        assertThat(ubuntu.getOS()).isEqualTo("ubuntu-14.04");
        assertThat(ubuntu.getCurrentVersion()).isEqualTo("3.8.1");
        assertThat(ubuntu.findVersion("3.8.1")).isNotNull();
        final DownloadVersion version = ubuntu.findVersion("3.0.5");
        assertThat(version).isNotNull();
        assertThat(version.getVersion()).isEqualTo("3.0.5");
        assertThat(version.getUrl())
                .isEqualTo("http://download.geteventstore.com/binaries/EventStore-OSS-Linux-v3.0.5.tar.gz");

    }

}
// CHECKSTYLE:ON
