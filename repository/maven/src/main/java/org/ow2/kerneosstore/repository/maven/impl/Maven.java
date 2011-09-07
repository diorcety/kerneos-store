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


import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;

import org.ow2.kerneosstore.repository.Repository;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.sonatype.aether.artifact.Artifact;

import java.net.URL;
import java.util.List;

@Component(name = Maven.COMPONENT_NAME)
@Provides
public class Maven implements Repository {
    /**
     * The logger.
     */
    private static Log logger = LogFactory.getLog(Maven.class);

    public static final String COMPONENT_NAME = "org.ow2.kerneosstore.repository.Maven";

    @Property(name = "ID", mandatory = true)
    @ServiceProperty(name = "ID")
    private Long ID;

    @Property(name = "repository", mandatory = false)
    private String repository;

    private MavenThread thread;

    private ArtifactResolver resolver;

    /**
     * Constructor
     */
    private Maven() {
    }

    /**
     * Validation of the component
     */
    @Validate
    private void start() {
        logger.debug("Start Maven Repository: " + ID);
        resolver = new ArtifactResolver(repository);

        thread = new MavenThread(ID, resolver);
        thread.start();
    }

    /**
     * Invalidation of the component
     */
    @Invalidate
    private void stop() {
        logger.debug("Stop Maven Repository: " + ID);
        if (thread != null) {
            try {
                thread.halt();
                thread.join();
                thread = null;
            } catch (Exception e) {

            }
        }
    }

    @Override
    public List getModules() {
        return (thread != null) ? thread.getModuleList() : null;
    }

    @Override
    public Object getModule(String repositoryKey) {
        try {
            return resolver.getArtifact(repositoryKey);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public URL getModuleURI(String repositoryKey) {
        try {
            Artifact artifact = resolver.getArtifact(repositoryKey);
            return artifact.getFile().toURI().toURL();
        } catch (Exception e) {
            logger.error("Can't get the artifact \"" + repositoryKey + "\": " + e);
            return null;
        }
    }
}
