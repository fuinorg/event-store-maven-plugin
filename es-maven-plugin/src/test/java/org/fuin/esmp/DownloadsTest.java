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
import java.net.URL;
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
        final URL versionURL = new URL(AbstractEventStoreMojo.VERSION_URL);
        final Downloads testee = new Downloads(versionURL,
                new File(Utils4J.getTempDir(), "event-store-versions-" + UUID.randomUUID() + ".json"));

        // TEST
        testee.parse();

        // VERIFY
        assertThat(testee.getVersions()).contains(new DownloadVersion("4.1.1"), new DownloadVersion("3.9.4"),
                new DownloadVersion("2.0.1"));
        final DownloadVersion version = testee.findVersion("4.1.0");
        assertThat(version.getName()).isEqualTo("4.1.0");
        assertThat(version.getOSFamilies()).hasSize(2);

    }

    @Test
    public void testLocal() throws IOException {

        // PREPARE
        final URL versionURL = new URL(AbstractEventStoreMojo.VERSION_URL);
        final Downloads testee = new Downloads(versionURL, new File("./target/test-classes/test-download.json"));

        // TEST
        testee.parse();

        // VERIFY
        final DownloadVersion version = testee.findVersion("4.1.1");
        assertThat(version.getName()).isEqualTo("4.1.1");
        final DownloadOSFamily family = version.findFamily("Linux");
        assertThat(family.getName()).isEqualTo("Linux");
        final DownloadOS download = family.findDownload("Ubuntu 14.04");
        assertThat(download.getName()).isEqualTo("Ubuntu 14.04");
        assertThat(download.getUrl()).isEqualTo("https://eventstore.org/downloads/EventStore-OSS-Ubuntu-14.04-v4.1.1.tar.gz");

    }
    
    @Test
    public void testFindLatest() throws IOException {
        
        // PREPARE
        final URL versionURL = new URL(AbstractEventStoreMojo.VERSION_URL);
        final Downloads testee = new Downloads(versionURL, new File("./target/test-classes/test-download.json"));
        testee.parse();
        
        // TEST & VERIFY
        assertThat(testee.findLatest(true).getName()).isEqualTo("5.0.0-rc1");
        assertThat(testee.findLatest(false).getName()).isEqualTo("4.1.1-hotfix1");
        
    }
    

}
// CHECKSTYLE:ON
