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
package org.fuin.esmp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A customizable source code generator plugin for maven.
 * 
 * @goal stop
 * @phase post-integration-test
 * @requiresProject false
 */
public final class EventStoreStopMojo extends AbstractEventStoreMojo {

    private static final Logger LOG = LoggerFactory
            .getLogger(EventStoreStopMojo.class);

    /**
     * Name of the executable or shell script to stops the event store. Defaults
     * to the OS specific name for Windows, Linux and Mac OS families. Other OS
     * families will cause an error if this value is not set.
     * 
     * @parameter expression="${command}"
     */
    private String command;

    @Override
    protected final void executeGoal() throws MojoExecutionException {
        init();
        LOG.info("command={}", command);

        final CommandLine cmdLine = new CommandLine(command);
        cmdLine.addArgument(readPid());
        final Executor executor = new DefaultExecutor();
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final PumpStreamHandler psh = new PumpStreamHandler(bos);
            executor.setStreamHandler(psh);
            executor.setWorkingDirectory(getEventStoreDir());
            final int result = executor.execute(cmdLine);
            if (result != 0) {
                throw new MojoExecutionException(
                        "Error stopping the event store: " + result);
            }
            final List<String> messages = asList(bos.toString());
            logDebug(messages);
            deletePid();
            LOG.info("Event store successfully stopped");

        } catch (final IOException ex) {
            throw new MojoExecutionException(
                    "Error executing the command line: " + cmdLine, ex);
        }

    }

    private void init() throws MojoExecutionException {

        // Supply variables that are OS dependent
        if (OS.isFamilyWindows()) {
            if (command == null) {
                // TODO Implement!
                throw new UnsupportedOperationException("Not implemented yet!");
            }
        } else if (OS.isFamilyUnix()) {
            if (command == null) {
                command = "kill";
            }
        } else if (OS.isFamilyMac()) {
            if (command == null) {
                command = "kill";
            }
        } else {
            if (command == null) {
                throw new MojoExecutionException(
                        "Unknown OS - You must use the 'command' parameter");
            }
        }

    }

    /**
     * Returns the name of the executable or shell script to stop the event
     * store.
     * 
     * @return Executable name.
     */
    public final String getCommand() {
        return command;
    }

    /**
     * Sets the name of the executable or shell script to stop the event store.
     * 
     * @param command
     *            Executable name to set.
     */
    public final void setCommand(final String command) {
        this.command = command;
    }

}
