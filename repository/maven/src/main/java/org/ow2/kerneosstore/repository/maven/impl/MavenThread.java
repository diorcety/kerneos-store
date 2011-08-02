/**
 * Kerneos
 * Copyright (C) 2011 Bull S.A.S.
 * Contact: jasmine@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.kerneosstore.repository.maven.impl;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResolutionException;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.version.Version;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenThread extends Thread {
    /**
     * The logger.
     */
    private static Log logger = LogFactory.getLog(MavenThread.class);

    private boolean halted = true;

    private int sleepTime = 60000;
    private Long id;

    private ArtifactResolver resolver;

    public MavenThread(Long id, ArtifactResolver resolver) {
        this();
        this.id = id;
        this.resolver = resolver;
    }

    private MavenThread() {

    }

    @Override
    public synchronized void start() {
        halted = false;
        super.start();
    }

    public synchronized void halt() {
        halted = true;
        synchronized (this) {
            notify();
        }
    }


    @Override
    public void run() {
        try {
            do {
                logger.info("============================================================");
                logger.info(MavenThread.class.getName());
                logger.info("ID: " + resolver.getRepository().getId());
                logger.info("URL: " + resolver.getRepository().getUrl());
                logger.info("------------------------------------------------------------");
                logger.info("NO INDEX");
                logger.info("============================================================");

                // Wait
                synchronized (this) {
                    wait(sleepTime);
                }

            }
            while (!halted);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<Artifact> getModuleList() {
        return null;
    }
}
