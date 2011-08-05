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

import org.apache.commons.io.IOUtils;
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
import org.ow2.kerneosstore.core.EJBStoreClient;
import org.ow2.kerneosstore.web.CategoryElement;
import org.ow2.kerneosstore.web.ModuleElement;
import org.ow2.kerneosstore.web.RSStoreClient;

import org.ow2.kerneosstore.web.StoreElement;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

@Component
@Provides
@Instantiate
@Path("/")
public class StoreClientImpl implements RSStoreClient {
    /**
     * The logger.
     */
    private static Log logger = LogFactory.getLog(StoreClientImpl.class);

    private Map<Long, org.ow2.kerneosstore.repository.Repository> repositories;

    @Requires
    private EJBStoreClient storeClient;

    private StoreClientImpl() {
        repositories = new HashMap<Long, org.ow2.kerneosstore.repository.Repository>();
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Component life-cycle
    ///////////////////////////////////////////////////////////////////////////

    @Validate
    private void start() throws ServletException, NamespaceException {
        logger.debug("Start Store Client - RS");
    }

    @Invalidate
    private void stop() {
        logger.debug("Stop Store Client - RS");
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
    /// Client functions
    ///////////////////////////////////////////////////////////////////////////
    @Override
    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_XML)
    public StoreElement getStore() {
        return new StoreElement(storeClient.getStore());
    }


    @Override
    @GET
    @Path("/modules/name/{filter}")
    @Produces(MediaType.APPLICATION_XML)
    public Collection getModulesByName(
            @PathParam("filter") String filter,
            @QueryParam("order") String order,
            @QueryParam("itemByPage") Integer itemByPage,
            @QueryParam("page") Integer page) {
        Collection modules = storeClient.getModulesByName(filter, order, itemByPage, page);

        List<ModuleElement> moduleList = new LinkedList<ModuleElement>();
        if (modules != null) {
            for (Iterator iterator = modules.iterator(); iterator.hasNext(); ) {
                ModuleVersion moduleVersion = (ModuleVersion) iterator.next();
                moduleList.add(new ModuleElement(moduleVersion));
            }

        }

        return moduleList;
    }

    @Override
    @GET
    @Path("/module/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public ModuleElement getModuleVersion(@PathParam("id") Long id) {
        return new ModuleElement(storeClient.getModuleVersion(id));
    }

    @GET
    @Path("/module/{id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] downloadModule(@PathParam("id") Long id) {
        byte[] data = null;
        for (Map.Entry<Repository, String> entry : storeClient.getRepositoryEntries(id).entrySet()) {
            org.ow2.kerneosstore.repository.Repository repo;
            synchronized (repositories) {
                repo = repositories.get(entry.getKey().getId());
            }
            if (repo != null) {
                URL url = repo.getModuleURI(entry.getValue());
                try {
                    InputStream stream = url.openStream();
                    data = IOUtils.toByteArray(stream);
                } catch (Exception e) {
                    logger.warn("Can't load the URL \"" + url + "\": " + e);
                }
            }
            if (data != null)
                return data;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @Override
    @GET
    @Path("/module/{id}/image")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] getModuleVersionImage(@PathParam("id") Long id) {
        return storeClient.getModuleVersionImage(id);
    }


    @Override
    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_XML)
    public Collection getCategories() {
        Collection<CategoryElement> categories = new LinkedList<CategoryElement>();
        for (Category category : storeClient.getCategories()) {
            categories.add(new CategoryElement(category));
        }
        return categories;
    }

    @Override
    @GET
    @Path("/category/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public CategoryElement getCategory(@PathParam("id") Long id) {
        Category category = storeClient.getCategory(id);
        CategoryElement categoryElement = new CategoryElement();
        categoryElement.setId(category.getId());
        categoryElement.setName(category.getName());
        categoryElement.setDescription(category.getDescription());

        return categoryElement;
    }

    @Override
    public Map getRepositoryEntries(Long moduleId) {
        return null;
    }
}
