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
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test for {@link DownloadOSFamily}.
 */
// CHECKSTYLE:OFF Test
public class DownloadOSFamilyTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(DownloadOSFamily.class).suppress(Warning.NULL_FIELDS, Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }

    @Test
    public void testConstruction() throws MalformedURLException {

        // PREPARE
        final String name = "Linux";
        final List<DownloadOS> downloads = new ArrayList<>();
        final DownloadOS v16 = new DownloadOS("Ubuntu 16.04 64-bit (.deb)",
                "https://eventstore.org/downloads/ubuntu/xenial/eventstore-oss_5.0.0-rc1-1_amd64.deb");
        final DownloadOS v18 = new DownloadOS("Ubuntu 18.04 64-bit (.deb)",
                "https://eventstore.org/downloads/ubuntu/bionic/eventstore-oss_5.0.0-rc1-1_amd64.deb");
        downloads.add(v16);
        downloads.add(v18);

        // TEST
        DownloadOSFamily testee = new DownloadOSFamily(name, downloads);

        // VERIFY
        assertThat(testee.getName()).isEqualTo(name);
        assertThat(testee.getDownloads()).containsExactlyInAnyOrder(v16, v18);

        // TEST
        testee = new DownloadOSFamily(name);

        // VERIFY
        assertThat(testee.getName()).isEqualTo(name);
        assertThat(testee.getDownloads()).isEmpty();

    }

    @Test
    public void testAdd() {

        // PREPARE
        final String name = "Linux";
        final DownloadOSFamily testee = new DownloadOSFamily(name);

        final DownloadOS v16 = new DownloadOS("Ubuntu 16.04 64-bit (.deb)",
                "https://eventstore.org/downloads/ubuntu/xenial/eventstore-oss_5.0.0-rc1-1_amd64.deb");
        final DownloadOS v18 = new DownloadOS("Ubuntu 18.04 64-bit (.deb)",
                "https://eventstore.org/downloads/ubuntu/bionic/eventstore-oss_5.0.0-rc1-1_amd64.deb");

        // TEST
        testee.addOS(v16);
        testee.addOS(v18);

        // VERIFY
        assertThat(testee.getName()).isEqualTo(name);
        assertThat(testee.getDownloads()).containsExactlyInAnyOrder(v16, v18);

    }

    @Test
    public void testSeal() {

        // PREPARE
        final String name = "Linux";
        final DownloadOSFamily testee = new DownloadOSFamily(name);

        final DownloadOS v16 = new DownloadOS("Ubuntu 16.04 64-bit (.deb)",
                "https://eventstore.org/downloads/ubuntu/xenial/eventstore-oss_5.0.0-rc1-1_amd64.deb");
        final DownloadOS v18 = new DownloadOS("Ubuntu 18.04 64-bit (.deb)",
                "https://eventstore.org/downloads/ubuntu/bionic/eventstore-oss_5.0.0-rc1-1_amd64.deb");
        testee.addOS(v16);

        // TEST & VERIFY
        testee.seal();
        try {
            testee.addOS(v18);
            fail();
        } catch (final IllegalStateException ex) {
            assertThat(ex.getMessage()).isEqualTo("The instance is sealed");
        }

    }

    @Test
    public void testFindDownload() {

        // PREPARE
        final String name = "Linux";
        final DownloadOSFamily testee = new DownloadOSFamily(name);

        final DownloadOS v16 = new DownloadOS("Ubuntu 16.04 64-bit (.deb)",
                "https://eventstore.org/downloads/ubuntu/xenial/eventstore-oss_5.0.0-rc1-1_amd64.deb");
        final DownloadOS v18 = new DownloadOS("Ubuntu 18.04 64-bit (.deb)",
                "https://eventstore.org/downloads/ubuntu/bionic/eventstore-oss_5.0.0-rc1-1_amd64.deb");
        testee.addOS(v16);
        testee.addOS(v18);
        testee.seal();

        // TEST
        final DownloadOS download = testee.findDownload("Ubuntu 16.04 64-bit (.deb)");
        
        // VERIFY
        assertThat(download).isSameAs(v16);


    }

    @Test
    public void testGetLatestDownload() {

        // PREPARE
        final String name = "Linux";
        final DownloadOSFamily testee = new DownloadOSFamily(name);
        testee.addOS(new DownloadOS("Ubuntu 14.04 64-bit (.deb)", "https://eventstore.org/downloads/ubuntu/trusty/eventstore-oss_5.0.0-rc1-1_amd64.deb"));
        testee.addOS(new DownloadOS("Ubuntu 16.04 64-bit (.deb)", "https://eventstore.org/downloads/ubuntu/xenial/eventstore-oss_5.0.0-rc1-1_amd64.deb"));
        testee.addOS(new DownloadOS("Ubuntu 18.04 64-bit (.deb)", "https://eventstore.org/downloads/ubuntu/bionic/eventstore-oss_5.0.0-rc1-1_amd64.deb"));
        testee.addOS(new DownloadOS("Linux 64-bit (mono 5.16.0.220)", "https://eventstore.org/downloads/el7/EventStore-OSS-Linux-Mono-v5.0.0-rc1.tar.gz"));
        testee.seal();

        // TEST & VERIFY
        assertThat(testee.findLatestDownload("Ubuntu").getName()).isEqualTo("Ubuntu 18.04 64-bit (.deb)");
        assertThat(testee.findLatestDownload("Linux").getName()).isEqualTo("Linux 64-bit (mono 5.16.0.220)");
        assertThat(testee.findLatestDownload("Ubuntu 16").getName()).isEqualTo("Ubuntu 16.04 64-bit (.deb)");
        assertThat(testee.findLatestDownload("Ubuntu 14").getName()).isEqualTo("Ubuntu 14.04 64-bit (.deb)");
        assertThat(testee.findLatestDownload(null).getName()).isEqualTo("Linux 64-bit (mono 5.16.0.220)");
        
    }
    
}
// CHECKSTYLE:ON
