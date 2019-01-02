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

import java.net.MalformedURLException;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test for {@link DownloadOS}.
 */
// CHECKSTYLE:OFF Test
public class DownloadOSTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(DownloadOS.class).suppress(Warning.NULL_FIELDS, Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }

    @Test
    public void testConstruction() throws MalformedURLException {

        // PREPARE
        final String name = "Ubuntu 18.04 64-bit (.deb)";
        final String url = "https://eventstore.org/downloads/ubuntu/bionic/eventstore-oss_5.0.0-rc1-1_amd64.deb";

        // TEST
        final DownloadOS testee = new DownloadOS(name, url);

        // VERIFY
        assertThat(testee.getName()).isEqualTo(name);
        assertThat(testee.getUrl()).isEqualTo(url);

    }

}
// CHECKSTYLE:ON
