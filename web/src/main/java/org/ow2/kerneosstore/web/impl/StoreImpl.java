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

import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;

import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import org.ow2.kerneosstore.api.IStoreAdmin;
import org.ow2.kerneosstore.api.IStoreClient;
import org.ow2.kerneosstore.core.Store;
import org.ow2.kerneosstore.repository.Repository;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.servlet.ServletException;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

@Component
@Instantiate
public class StoreImpl extends Application implements IStoreClient, IStoreAdmin {

    /**
     * The logger.
     */
    private static Log logger = LogFactory.getLog(StoreImpl.class);

    private Map<Long, Repository> repositories;

    @Property(name = "context", value = "/store")
    private String context;

    @Requires
    private HttpService httpService;

    @Requires
    private Store store;

    private StoreImpl() {
        repositories = new HashMap<Long, Repository>();
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Component life-cycle
    ///////////////////////////////////////////////////////////////////////////

    @Validate
    private void start() throws ServletException, NamespaceException {
        logger.debug("Start Store WEB-RS: " + context);

        httpService.registerServlet(context, new ServletContainer(this), null, null);
    }

    @Invalidate
    private void stop() {
        logger.debug("Stop Store WEB-RS: " + context);
        httpService.unregister(context);
    }

    @Bind(optional = true, aggregate = true)
    private void bindRepository(Repository repository, ServiceReference sr) {
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
    private void unbindRepository(Repository repository, ServiceReference sr) {
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
    /// JAXRS Application
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> objects = new HashSet<Class<?>>();
        objects.add(EJBExceptionMapper.class);
        return objects;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = new HashSet<Object>();
        objects.add(this);
        return objects;
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Client functions
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public org.ow2.kerneosstore.api.StoreInfo getInfo() {
        return StoreUtil.EJB_2_API(store.getStoreInfo());
    }

    @Override
    public org.ow2.kerneosstore.api.Modules getModules(String filter, String order, String fieldName, Integer itemByPage, Integer page) {
        Collection<org.ow2.kerneosstore.core.ModuleVersion> modules = null;
        if (fieldName == null) {
            modules = store.getModulesByName(filter, order, itemByPage, page);
        } else if (fieldName.equals("name")) {
            modules = store.getModulesByName(filter, order, itemByPage, page);
        }

        // Transform to array
        if (modules != null) {
            List<org.ow2.kerneosstore.api.Module> moduleList = new LinkedList<org.ow2.kerneosstore.api.Module>();
            for (Iterator<org.ow2.kerneosstore.core.ModuleVersion> iterator = modules.iterator(); iterator.hasNext(); ) {
                moduleList.add(StoreUtil.EJB_2_API(iterator.next()));
            }
            return new org.ow2.kerneosstore.api.Modules(moduleList);
        }

        return new org.ow2.kerneosstore.api.Modules();
    }

    @Override
    public org.ow2.kerneosstore.api.Modules getUpdatedModules(org.ow2.kerneosstore.api.Modules modules) {
        return null;
    }

    @Override
    public org.ow2.kerneosstore.api.Module getModule(Long id) {
        org.ow2.kerneosstore.core.ModuleVersion moduleVersion = store.getModuleVersion(id, null, null, null);
        return StoreUtil.EJB_2_API(moduleVersion);
    }


    @Override
    public byte[] downloadModule(Long id) {
        byte[] data = null;
        for (org.ow2.kerneosstore.core.RepositoryEntity repositoryEntity : store.getRepositoryEntities(id, null, null, null)) {
            Repository repo;
            synchronized (repositories) {
                repo = repositories.get(repositoryEntity.getRepository().getId());
            }
            if (repo != null) {
                URL url = repo.getModuleURI(repositoryEntity.getKey());
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
    public byte[] getModuleImage(Long id) {
        org.ow2.kerneosstore.core.ModuleVersion moduleVersion = store.getModuleVersion(id, null, null, null);
        return moduleVersion.getImage();
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Admin functions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public org.ow2.kerneosstore.api.StoreInfo setInfo(org.ow2.kerneosstore.api.StoreInfo storeInfo) {
        return StoreUtil.EJB_2_API(store.setStoreInfo(StoreUtil.API_2_EJB(storeInfo)));
    }

    @Override
    public org.ow2.kerneosstore.api.Module setModule(org.ow2.kerneosstore.api.Module module) {
        org.ow2.kerneosstore.core.ModuleVersion moduleVersion = StoreUtil.API_2_EJB(module);
        if (module.getId() != null) {
            moduleVersion.setModule(store.getModule(module.getId()));
        } else {
            moduleVersion.setModule(store.setModule(new org.ow2.kerneosstore.core.Module()));
        }

        store.setModuleVersion(moduleVersion);
        return StoreUtil.EJB_2_API(moduleVersion);
    }

    @Override
    public org.ow2.kerneosstore.api.Module getModule(Long id, Integer major, Integer minor, Integer revision) {
        org.ow2.kerneosstore.core.ModuleVersion moduleVersion = store.getModuleVersion(id, major, minor, revision);
        return StoreUtil.EJB_2_API(moduleVersion);
    }

    @Override
    public org.ow2.kerneosstore.api.Modules getModules(Long id) {
        org.ow2.kerneosstore.core.Module module = store.getModule(id);
        List<org.ow2.kerneosstore.api.Module> moduleList = new LinkedList<org.ow2.kerneosstore.api.Module>();
        for (Iterator<org.ow2.kerneosstore.core.ModuleVersion> iterator = module.getVersions().iterator(); iterator.hasNext(); ) {
            moduleList.add(StoreUtil.EJB_2_API(iterator.next()));
        }
        return new org.ow2.kerneosstore.api.Modules(moduleList);
    }

    @Override
    public org.ow2.kerneosstore.api.Module enableModule(Long id, Integer major, Integer minor, Integer revision) {
        org.ow2.kerneosstore.core.ModuleVersion moduleVersion = store.getModuleVersion(id, major, minor, revision);
        moduleVersion.setAvailable(true);
        moduleVersion = store.setModuleVersion(moduleVersion);
        return StoreUtil.EJB_2_API(moduleVersion);
    }

    @Override
    public org.ow2.kerneosstore.api.Module disableModule(Long id, Integer major, Integer minor, Integer revision) {
        org.ow2.kerneosstore.core.ModuleVersion moduleVersion = store.getModuleVersion(id, major, minor, revision);
        moduleVersion.setAvailable(false);
        moduleVersion = store.setModuleVersion(moduleVersion);
        return StoreUtil.EJB_2_API(moduleVersion);
    }

    @Override
    public org.ow2.kerneosstore.api.Module removeModule(Long id, Integer major, Integer minor, Integer revision) {
        return StoreUtil.EJB_2_API(store.removeModuleVersion(id, major, minor, revision));
    }

    @Override
    public org.ow2.kerneosstore.api.Repository getRepository(Long id) {
        return StoreUtil.EJB_2_API(store.getRepository(id));
    }

    @Override
    public org.ow2.kerneosstore.api.Repository removeRepository(Long id) {
        return StoreUtil.EJB_2_API(store.removeRepository(id));
    }

    @Override
    public org.ow2.kerneosstore.api.Repository setRepository(org.ow2.kerneosstore.api.Repository repository) {
        return StoreUtil.EJB_2_API(store.setRepository(StoreUtil.API_2_EJB(repository)));
    }

    @Override
    public String setRepositoryEntity(Long repositoryId, Long id, Integer major, Integer minor, Integer revision, String key) {
        org.ow2.kerneosstore.core.RepositoryEntity repositoryEntity = new org.ow2.kerneosstore.core.RepositoryEntity();
        repositoryEntity.setRepository(store.getRepository(repositoryId));
        repositoryEntity.setModuleVersion(store.getModuleVersion(id, major, minor, revision));
        repositoryEntity.setKey(key);
        store.setRepositoryEntity(repositoryEntity);
        return key;
    }

    @Override
    public String removeRepositoryEntity(Long repositoryId, Long id, Integer major, Integer minor, Integer revision) {
        return store.removeRepositoryEntity(repositoryId, id, major, minor, revision).getKey();
    }

    @Override
    public byte[] getModuleImage(Long id, Integer major, Integer minor, Integer revision) {
        org.ow2.kerneosstore.core.ModuleVersion moduleVersion = store.getModuleVersion(id, major, minor, revision);
        if (moduleVersion.getImage() == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        return moduleVersion.getImage();
    }

    @Override
    public byte[] setModuleImage(Long id, Integer major, Integer minor, Integer revision, byte[] data) {
        org.ow2.kerneosstore.core.ModuleVersion moduleVersion = store.getModuleVersion(id, major, minor, revision);
        moduleVersion.setImage(data);
        moduleVersion = store.setModuleVersion(moduleVersion);
        return moduleVersion.getImage();
    }

    @Override
    public byte[] removeModuleImage(Long id, Integer major, Integer minor, Integer revision) {
        org.ow2.kerneosstore.core.ModuleVersion moduleVersion = store.getModuleVersion(id, major, minor, revision);
        byte[] data = moduleVersion.getImage();
        moduleVersion.setImage(null);
        moduleVersion = store.setModuleVersion(moduleVersion);
        return data;
    }
}
