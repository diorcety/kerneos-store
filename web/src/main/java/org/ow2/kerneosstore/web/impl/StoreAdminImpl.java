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

package org.ow2.kerneosstore.web.impl;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;

import org.osgi.framework.ServiceReference;
import org.osgi.service.http.NamespaceException;

import org.ow2.kerneosstore.api.Category;
import org.ow2.kerneosstore.api.ModuleVersion;
import org.ow2.kerneosstore.api.Repository;
import org.ow2.kerneosstore.api.Store;
import org.ow2.kerneosstore.core.EJBStoreAdmin;
import org.ow2.kerneosstore.web.ModuleElement;
import org.ow2.kerneosstore.web.RepositoryElement;
import org.ow2.kerneosstore.web.RSStoreAdmin;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Component
@Provides
@Instantiate
@Path("/admin")
public class StoreAdminImpl implements RSStoreAdmin {
    /**
     * The logger.
     */
    private static Log logger = LogFactory.getLog(StoreAdminImpl.class);

    private Map<Long, org.ow2.kerneosstore.repository.Repository> repositories;

    @Requires
    private EJBStoreAdmin storeAdmin;

    private StoreAdminImpl() {
        repositories = new HashMap<Long, org.ow2.kerneosstore.repository.Repository>();
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Component life-cycle
    ///////////////////////////////////////////////////////////////////////////

    @Validate
    private void start() throws ServletException, NamespaceException {
        logger.debug("Start Store Admin - RS");
    }

    @Invalidate
    private void stop() {
        logger.debug("Stop Store Admin - RS");
    }

    @Bind(optional = true, aggregate = true)
    private void bindRepository(org.ow2.kerneosstore.repository.Repository repository, ServiceReference sr) {
        try {
            Long id = (Long) sr.getProperty("ID");
            if (id != null) {
                synchronized (repositories) {
                    repositories.put(id, repository);
                }
            } else {
                logger.error("Invalid repository service: ID property has to be defined");
            }
        } catch (Exception e) {
            logger.error("Invalid repository service: ID property has to be a Long");
        }
    }

    @Unbind
    private void unbindRepository(org.ow2.kerneosstore.repository.Repository repository, ServiceReference sr) {
        try {
            String id = (String) sr.getProperty("ID");
            if (id != null) {
                synchronized (repositories) {
                    repositories.remove(id);
                }
            } else {
                logger.error("Invalid repository service: ID property has to be defined");
            }
        } catch (Exception e) {
            logger.error("Invalid repository service: ID property has to be a String");
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    /// Admin functions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    @POST
    @Path("/info")
    @Consumes(MediaType.APPLICATION_XML)
    public void setStore(Store store) {
        storeAdmin.setStore(store);
    }


    @POST
    @Path("/module")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public Long setModuleVersion(ModuleVersion module) {
        return storeAdmin.setModuleVersion(module);
    }

    @Override
    @GET
    @Path("/module/{id}/{major}/{minor}/{revision}")
    @Produces(MediaType.APPLICATION_XML)
    public ModuleElement getModuleVersion(@PathParam("id") Long id,
                                          @PathParam("major") Integer major,
                                          @PathParam("minor") Integer minor,
                                          @PathParam("revision") Integer revision) {
        return new ModuleElement(storeAdmin.getModuleVersion(id, major, minor, revision));
    }

    @Override
    @GET
    @Path("/modules/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Collection getModules(@PathParam("id") Long id) {
        Collection<ModuleElement> moduleElements = new LinkedList<ModuleElement>();
        for (ModuleVersion moduleVersion : storeAdmin.getModules(id)) {
            moduleElements.add(new ModuleElement(moduleVersion));
        }
        return moduleElements;
    }

    @Override
    @GET
    @Path("/module/{id}/{major}/{minor}/{revision}/enable")
    public void enableModuleVersion(@PathParam("id") Long id,
                                    @PathParam("major") Integer major,
                                    @PathParam("minor") Integer minor,
                                    @PathParam("revision") Integer revision) {
        storeAdmin.enableModuleVersion(id, major, minor, revision);
    }

    @Override
    @GET
    @Path("/module/{id}/{major}/{minor}/{revision}/disable")
    public void disableModuleVersion(@PathParam("id") Long id,
                                     @PathParam("major") Integer major,
                                     @PathParam("minor") Integer minor,
                                     @PathParam("revision") Integer revision) {
        storeAdmin.disableModuleVersion(id, major, minor, revision);
    }

    @Override
    @DELETE
    @Path("/module/{id}/{major}/{minor}/{revision}")
    @Produces(MediaType.APPLICATION_XML)
    public void removeModuleVersion(@PathParam("id") Long id,
                                    @PathParam("major") Integer major,
                                    @PathParam("minor") Integer minor,
                                    @PathParam("revision") Integer revision) {
        storeAdmin.removeModuleVersion(id, major, minor, revision);
    }

    @Override
    @GET
    @Path("/repository/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public RepositoryElement getRepository(@PathParam("id") Long id) {
        return new RepositoryElement(storeAdmin.getRepository(id));
    }

    @Override
    @DELETE
    @Path("/repository/{id}")
    public void removeRepository(@PathParam("id") Long id) {
        storeAdmin.removeRepository(id);
    }


    @POST
    @Path("/repository")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public Long setRepository(Repository repository) {
        return storeAdmin.setRepository(repository);
    }

    @Override
    @POST
    @Path("/repository/{repositoryId}/module/{moduleId}/{major}/{minor}/{revision}")
    @Consumes(MediaType.TEXT_PLAIN)
    public void setRepositoryEntry(@PathParam("repositoryId") Long repositoryId,
                                   @PathParam("moduleId") Long id,
                                   @PathParam("major") Integer major,
                                   @PathParam("minor") Integer minor,
                                   @PathParam("revision") Integer revision,
                                   String key) {
        storeAdmin.setRepositoryEntry(repositoryId, id, major, minor, revision, key);
    }

    @Override
    @DELETE
    @Path("/repository/{repositoryId}/module/{moduleId}/{major}/{minor}/{revision}")
    public void removeRepositoryEntry(@PathParam("repositoryId") Long repositoryId,
                                      @PathParam("moduleId") Long id,
                                      @PathParam("major") Integer major,
                                      @PathParam("minor") Integer minor,
                                      @PathParam("revision") Integer revision) {
        storeAdmin.removeRepositoryEntry(repositoryId, id, major, minor, revision);
    }

    @Override
    @GET
    @Path("/module/{id}/{major}/{minor}/{revision}/image")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] getModuleVersionImage(@PathParam("id") Long id,
                                        @PathParam("major") Integer major,
                                        @PathParam("minor") Integer minor,
                                        @PathParam("revision") Integer revision) {
        return storeAdmin.getModuleVersionImage(id, major, minor, revision);
    }

    @Override
    @POST
    @Path("/module/{id}/{major}/{minor}/{revision}/image")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void setModuleVersionImage(@PathParam("id") Long id,
                                      @PathParam("major") Integer major,
                                      @PathParam("minor") Integer minor,
                                      @PathParam("revision") Integer revision, byte[] data) {
        storeAdmin.setModuleVersionImage(id, major, minor, revision, data);
    }

    @Override
    @DELETE
    @Path("/module/{id}/{major}/{minor}/{revision}/image")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public void removeModuleVersionImage(@PathParam("id") Long id,
                                         @PathParam("major") Integer major,
                                         @PathParam("minor") Integer minor,
                                         @PathParam("revision") Integer revision) {
        storeAdmin.removeModuleVersionImage(id, major, minor, revision);
    }


    @Override
    @DELETE
    @Path("/category/{id}")
    public void removeCategory(@PathParam("id") Long id) {
        storeAdmin.removeCategory(id);
    }


    @POST
    @Path("/category")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public Long setCategory(Category category) {
        return storeAdmin.setCategory(category);
    }

    @Override
    @POST
    @Path("/category/{categoryId}/module/{moduleId}")
    public void addModuleToCategory(@PathParam("categoryId") Long categoryId, @PathParam("moduleId") Long moduleId) {
        storeAdmin.addModuleToCategory(categoryId, moduleId);
    }

    @Override
    @DELETE
    @Path("/category/{categoryId}/module/{moduleId}")
    public void removeModuleFromCategory(@PathParam("categoryId") Long categoryId, @PathParam("moduleId") Long moduleId) {
        storeAdmin.removeModuleFromCategory(categoryId, moduleId);
    }

    @Override
    public Collection<Repository> getRepositoriesByType(String type) {
        return null;
    }

    @Override
    public Map getRepositoryEntries(Long moduleId, Integer major, Integer minor, Integer revision) {
        return null;
    }

    @Override
    public void removeModule(Long id) {
    }
}
