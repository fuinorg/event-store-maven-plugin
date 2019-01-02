/**
 * Copyright (C) 2015 Michael Schnell. All rights reserved. 
 * http://www.fuin.org/
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either name 3 of the License, or (at your option) any
 * later name.
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
 * OS name of the Event Store that is available for download. Equals and hash code are based on the name.
 */
public final class DownloadOS implements Comparable<DownloadOS> {

    private final String name;

    private final String url;

    /**
     * Constructor with mandatory data.
     * 
     * @param name
     *            Name (like "3.1.0").
     */
    public DownloadOS(final String name) {
        this(name, "http://localhost/none.json");
    }    
    
    /**
     * Constructor with all data.
     * 
     * @param name
     *            Name (like "3.1.0").
     * @param url
     *            Download URL.
     */
    public DownloadOS(final String name, final String url) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("name == null");
        }
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        this.name = name;
        this.url = url;
    }

    /**
     * Returns the name.
     * 
     * @return Name (like "Windows 64-bit (.NET 4.7.1+)", "Ubuntu 14.04", "Ubuntu 18.04 64-bit (.deb)").
     */
    public final String getName() {
        return name;
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
        return name.hashCode();
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
        final DownloadOS other = (DownloadOS) obj;
        return name.equals(other.name);
    }

    @Override
    public final String toString() {
        return name;
    }

    @Override
    public final int compareTo(final DownloadOS other) {
        return name.compareTo(other.name);
    }

}
