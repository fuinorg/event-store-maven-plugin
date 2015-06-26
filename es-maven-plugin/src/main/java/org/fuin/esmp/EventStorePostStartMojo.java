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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Executes a script after starting the event store.
 *
 */
@Mojo(name = "post-start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresProject = false)
public final class EventStorePostStartMojo extends AbstractEventStoreMojo {

    private static final Logger LOG = LoggerFactory
            .getLogger(EventStorePostStartMojo.class);

    /**
     * Name of an executable or shell script which is executed after successful
     * start the event store.
     *
     */
    @Parameter(name = "postStartCommand", required = true)
    private String postStartCommand;
    
    /** Messages from last execution. */
    private List<String> messages;

    @Override
    protected final void executeGoal() throws MojoExecutionException {
        if (postStartCommand == null) {
            throw new MojoExecutionException("postStartCommand not set");
        }
        LOG.info("postStartCommand={}", postStartCommand);

        if (OS.isFamilyUnix() || OS.isFamilyMac()) {
            final File execFile = new File(getEventStoreDir(), postStartCommand);
            execFile.setExecutable(true);
        }

        final CommandLine cmdLine = new CommandLine(postStartCommand);
        final DefaultExecutor executor = new DefaultExecutor();
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final PumpStreamHandler psh = new PumpStreamHandler(bos);
            executor.setStreamHandler(psh);
            executor.setWorkingDirectory(getEventStoreDir());
            final int exitCode = executor.execute(cmdLine);
            messages = asList(bos.toString());            
            if (exitCode == 0) {
                LOG.info("Post-start command executed successfully");
                logDebug(messages);
            } else {
                LOG.error("Post-start command failed with exit code: {}",
                        exitCode);
                logError(messages);
            }
        } catch (final IOException ex) {
            throw new MojoExecutionException(
                    "Error executing the command line: " + cmdLine, ex);
        }
    }

    /**
     * Returns the name of the executable or shell script to execute after
     * starting the event store.
     *
     * @return Executable name.
     */
    public final String getPostStartCommand() {
        return postStartCommand;
    }

    /**
     * Sets the name of the executable or shell script to execute after starting
     * the event store.
     *
     * @param postStartCommand
     *            Executable name to set.
     */
    public final void setPostStartCommand(final String postStartCommand) {
        this.postStartCommand = postStartCommand;
    }
    
    /**
     * Returns the messages from the last execution.
     * 
     * @return Messages.
     */
    public final List<String> getMessages() {
        return messages;
    }

}
