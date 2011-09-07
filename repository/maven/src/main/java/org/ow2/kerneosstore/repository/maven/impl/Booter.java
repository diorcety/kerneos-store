package org.ow2.kerneosstore.repository.maven.impl;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.apache.maven.repository.internal.MavenRepositorySystemSession;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * A helper to boot the repository system and a repository system session.
 */
public class Booter {

    public static RepositorySystem newRepositorySystem() {
        return ManualRepositorySystemFactory.newRepositorySystem();
    }

    public static RepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        return newRepositorySystemSession(system, "target/local-repo");
    }

    public static RepositorySystemSession newRepositorySystemSession(RepositorySystem system, String repo) {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository(repo);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

        return session;
    }

    public static RemoteRepository newCentralRepository() {
        return new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/");
    }

}
