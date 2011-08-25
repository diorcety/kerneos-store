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
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.ow2.kerneosstore.api.Repository;
import org.ow2.kerneosstore.core.EJBStoreAdmin;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

@Component
@Instantiate
public class Activator {
    /**
     * The logger.
     */
    private static Log logger = LogFactory.getLog(Activator.class);

    @Requires
    private EJBStoreAdmin store;

    @Requires
    private ConfigurationAdmin configurationAdmin;

    private ActivatorThread thread;

    private Map<Repository, Configuration> configurationMap = new HashMap<Repository, Configuration>();

    @Validate
    private void start() {
        logger.debug("Start Mavan Repository Activator");
        thread = new ActivatorThread();
        thread.start();
    }

    @Invalidate
    private void stop() {
        logger.debug("Stop Mavan Repository Activator");
        try {
            thread.halt();
            thread.join();
            thread = null;
        } catch (Exception e) {
            logger.error("Can't correctly stop the activator thread: " + e);
        }
    }

    private synchronized void addRepository(Repository repository) {
        try {
            Dictionary properties = new Hashtable();
            for (Map.Entry<String, String> e : repository.getProperties().entrySet()) {
                properties.put(e.getKey(), e.getValue());
            }
            properties.put("ID", repository.getId());
            Configuration cfg = configurationAdmin.createFactoryConfiguration(Maven.COMPONENT_NAME, null);
            cfg.update(properties);
            configurationMap.put(repository, cfg);
        } catch (Exception e) {
            logger.error("Can't create the repository \"" + repository.getName() + "\"(" + repository.getId() + "): " + e);
        }
    }

    private synchronized void removeRepository(Repository repository) {
        try {
            Configuration cfg = configurationMap.remove(repository);
            cfg.delete();
        } catch (Exception e) {
            logger.error("Can't remove the repository \"" + repository.getName() + "\"(" + repository.getId() + "): " + e);
        }
    }

    class ActivatorThread extends Thread {
        private int sleepTime = 10000;

        private boolean halted = true;

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

        private boolean contains(Collection<? extends Repository> repositories, Repository repository) {
            // Check repository modification
            for (Repository rep : repositories) {
                if (rep.getId().equals(repository.getId()) &&
                        rep.getName().equals(repository.getName()) &&
                        rep.getType().equals(repository.getType()) &&
                        rep.getProperties().equals(repository.getProperties())
                        )
                    return true;
            }
            return false;
        }

        @Override
        public void run() {
            try {
                do {
                    Collection<? extends Repository> mavenRepositories = store.getRepositoriesByType(Maven.COMPONENT_NAME);

                    // Remove old repositories
                    for (Iterator<Repository> repositoryIt = configurationMap.keySet().iterator(); repositoryIt.hasNext(); ) {
                        Repository repository = repositoryIt.next();
                        if (contains(mavenRepositories, repository)) {
                            mavenRepositories.remove(repository);
                        } else {
                            removeRepository(repository);
                        }
                    }

                    // Add new repositories
                    for (Repository repository : mavenRepositories) {
                        addRepository(repository);
                    }

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
    }
}
