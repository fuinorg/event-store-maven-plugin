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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Version of the Event Store that is available for download. Equals and hash
 * code are based on the version.
 */
public final class DownloadVersion implements Comparable<DownloadVersion> {

    private final String version;

    private final String url;

    /**
     * Constructor with mandatory data.
     * 
     * @param version
     *            Version (like "3.1.0").
     * @param url
     *            Download URL.
     */
    public DownloadVersion(final String version, final String url) {
        super();
        if (version == null) {
            throw new IllegalArgumentException("version == null");
        }
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        this.version = version;
        this.url = url;
    }

    /**
     * Returns the version.
     * 
     * @return Version (like "3.1.0").
     */
    public final String getVersion() {
        return version;
    }

    /**
     * Returns the download URL string.
     * 
     * @return Archive file URL string.
     */
    public final String getUrl() {
        return url;
    }

    /**
     * Returns the download URL.
     * 
     * @return Archive file URL.
     */
    public final URL getURL() {
        try {
            return new URL(url);
        } catch (final MalformedURLException ex) {
            throw new RuntimeException("Cannot convert URL: " + url, ex);
        }
    }

    @Override
    public final int hashCode() {
        return version.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DownloadVersion other = (DownloadVersion) obj;
        return version.equals(other.version);
    }

    @Override
    public final String toString() {
        return version;
    }

    @Override
    public final int compareTo(final DownloadVersion other) {
        return version.compareTo(other.version);
    }

}
