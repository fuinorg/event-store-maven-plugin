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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Operation system family of the Event Store that is available for download. Equals and hash code are based on the family name.
 */
public final class DownloadOSFamily implements Comparable<DownloadOSFamily> {

    private final String name;

    private final List<DownloadOS> downloads;

    private boolean sealed;

    /**
     * Constructor with mandatory data.
     * 
     * @param name
     *            Family name (like "Windows", "Mac" or "Linux").
     */
    public DownloadOSFamily(final String name) {
        this(name, new ArrayList<>());
    }

    /**
     * Constructor with mandatory data.
     * 
     * @param name
     *            Family name (like "Windows", "Mac" or "Linux").
     * @param downloads
     *            List of all downloads.
     */
    public DownloadOSFamily(final String name, final List<DownloadOS> downloads) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("name == null");
        }
        if (downloads == null) {
            throw new IllegalArgumentException("downloads == null");
        }
        this.name = name;
        this.downloads = new ArrayList<>(downloads);
    }

    /**
     * Returns the OS family name.
     * 
     * @return Family name (like "Windows", "Mac" or "Linux").
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the list of available downloads.
     * 
     * @return Unmodifiable list of downloads.
     */
    public final List<DownloadOS> getDownloads() {
        return Collections.unmodifiableList(downloads);
    }

    /**
     * Adds a new download to the list.
     * 
     * @param download
     *            Download to add.
     */
    final void addOS(final DownloadOS download) {
        if (sealed) {
            throw new IllegalStateException("The instance is sealed");
        }
        downloads.add(download);
    }

    /**
     * The instance is sealed and no changes are allowed any more.
     */
    final void seal() {
        Collections.sort(downloads, new Comparator<DownloadOS>() {
            @Override
            public int compare(DownloadOS o1, DownloadOS o2) {
                return o1.compareTo(o2) * -1;
            }
        });
        sealed = true;
    }

    /**
     * Returns the download with a given name.
     * 
     * @param name
     *            Download to find.
     * 
     * @return Found instance or <code>null</code> if no download with that name was found.
     */
    public DownloadOS findDownload(final String name) {
        final int idx = downloads.indexOf(new DownloadOS(name));
        if (idx < 0) {
            return null;
        }
        return downloads.get(idx);
    }

    /**
     * Returns the latest download version.
     * 
     * @param qualifier Type to select like "Ubuntu" or "Linux" or {@code null} (any).
     * 
     * @return Latest download.
     */
    public DownloadOS findLatestDownload(final String qualifier) {
        for (final DownloadOS download : downloads) {
            if (qualifier == null || download.getName().contains(qualifier)) {
                return download;
            }
        }
        throw new IllegalStateException("No downloads found for qualifier '" + qualifier + "'");
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
        final DownloadOSFamily other = (DownloadOSFamily) obj;
        return name.equals(other.name);
    }

    @Override
    public final int compareTo(final DownloadOSFamily other) {
        return name.compareTo(other.name);
    }

    @Override
    public final String toString() {
        return name;
    }

}
