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

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResolutionException;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArtifactResolver {
    private static Pattern artifactPattern = Pattern.compile("([^:]+):([^:]+)(?::([^:]+))?");

    private RemoteRepository repository = null;
    private RepositorySystem system;
    private RepositorySystemSession session;

    public ArtifactResolver(String repository) {
        // Init repository system
        this.system = Booter.newRepositorySystem();

        if (repository != null) {
            if (repository.toLowerCase().startsWith("file://")) {
                this.session = Booter.newRepositorySystemSession(this.system, repository.substring(7));
            } else {
                this.session = Booter.newRepositorySystemSession(this.system);
                this.repository = new RemoteRepository("default", "default", repository);
            }
        } else {
            this.session = Booter.newRepositorySystemSession(this.system);
            this.repository = Booter.newCentralRepository();
        }
    }

    public RepositorySystemSession getSession() {
        return session;
    }

    public RemoteRepository getRepository() {
        return repository;
    }

    /**
     * Get an artifact from repositories
     *
     * @return the artifact from a maven repository
     */
    public Artifact getArtifact(String repositoryKey) throws ArtifactResolutionException, VersionRangeResolutionException {
        // Parse repository key
        Matcher matcher = artifactPattern.matcher(repositoryKey);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid repositoryKey format");
        }
        String groupeId = matcher.group(1);
        String artifactId = matcher.group(2);
        String version = matcher.group(3);

        if (version == null) {
            // Build version range request
            Artifact artifact = new DefaultArtifact(groupeId, artifactId, "jar", "[0,)");
            VersionRangeRequest rangeRequest = new VersionRangeRequest();
            rangeRequest.setArtifact(artifact);

            // Add repository
            if (repository != null)
                rangeRequest.addRepository(repository);

            // Resolve
            VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);
            version = rangeResult.getHighestVersion().toString();
        }

        // Build artifact request
        Artifact artifact = new DefaultArtifact(groupeId, artifactId, "jar", version);
        ArtifactRequest artfactRequest = new ArtifactRequest();
        artfactRequest.setArtifact(artifact);

        // Add repository
        if (repository != null)
            artfactRequest.addRepository(repository);

        // Resolve
        ArtifactResult descriptorResult = system.resolveArtifact(session, artfactRequest);
        return descriptorResult.getArtifact();
    }
}
