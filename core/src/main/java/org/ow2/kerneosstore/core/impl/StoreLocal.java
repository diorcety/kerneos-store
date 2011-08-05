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

package org.ow2.kerneosstore.core.impl;

import org.ow2.kerneosstore.api.Category;
import org.ow2.kerneosstore.api.ModuleVersion;
import org.ow2.kerneosstore.api.Repository;
import org.ow2.kerneosstore.api.Store;
import org.ow2.kerneosstore.core.CategoryBean;
import org.ow2.kerneosstore.core.EJBStoreAdmin;
import org.ow2.kerneosstore.core.EJBStoreClient;
import org.ow2.kerneosstore.core.ModuleBean;
import org.ow2.kerneosstore.core.ModuleVersionBean;
import org.ow2.kerneosstore.core.ModuleVersionPK;
import org.ow2.kerneosstore.core.RepositoryBean;
import org.ow2.kerneosstore.core.RepositoryEntityBean;
import org.ow2.kerneosstore.core.RepositoryEntityPK;
import org.ow2.kerneosstore.core.StoreBean;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Stateless
public class StoreLocal implements EJBStoreClient, EJBStoreAdmin {

    /**
     * Entity manager used by this session bean.
     */
    @PersistenceContext
    private EntityManager entityManager = null;

    ///////////////////////////////////////////////////////////////////////////
    /// Store
    ///////////////////////////////////////////////////////////////////////////

    public StoreBean getStore() {
        String strQuery = "SELECT x FROM Store x";

        // Create the query
        Query q = entityManager.createQuery(strQuery);

        return (StoreBean) q.getSingleResult();
    }

    @Override
    public void setStore(Store store) {
        StoreBean storeBean = new StoreBean();
        storeBean.setName(store.getName());
        storeBean.setDescription(store.getDescription());
        storeBean.setUrl(store.getUrl());
        entityManager.merge(storeBean);
    }


    ///////////////////////////////////////////////////////////////////////////
    /// Module
    ///////////////////////////////////////////////////////////////////////////

    public ModuleBean getModule(Long id) {
        String strQuery = "SELECT x FROM Module x WHERE x.id = :id";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("id", id);

        ModuleBean module = (ModuleBean) q.getSingleResult();

        // Force EAGER
        module.getVersions().size();
        module.getCategories().size();

        return module;
    }

    @Override
    public Collection<ModuleVersion> getModules(Long id) {
        String strQuery = "SELECT x FROM Module x WHERE x.id = :id";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("id", id);

        ModuleBean module = (ModuleBean) q.getSingleResult();

        // Force EAGER
        module.getVersions().size();
        return module.getVersions();
    }

    @Override
    public void removeModule(Long id) {
        ModuleBean module = entityManager.find(ModuleBean.class, id);
        entityManager.remove(module);
    }


    ///////////////////////////////////////////////////////////////////////////
    /// ModuleVersion
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void enableModuleVersion(Long id, Integer major, Integer minor, Integer revision) {
        ModuleVersionBean moduleVersion = getModuleVersion(id, major, minor, revision);
        moduleVersion.setAvailable(true);
    }

    @Override
    public void disableModuleVersion(Long id, Integer major, Integer minor, Integer revision) {
        ModuleVersionBean moduleVersion = getModuleVersion(id, major, minor, revision);
        moduleVersion.setAvailable(false);
    }

    @Override
    public byte[] getModuleVersionImage(Long id) {
        return getModuleVersionImage(id, null, null, null);
    }

    @Override
    public byte[] getModuleVersionImage(Long id, Integer major, Integer minor, Integer revision) {
        return getModuleVersion(id, major, minor, revision).getImage();
    }

    @Override
    public void setModuleVersionImage(Long id, Integer major, Integer minor, Integer revision, byte[] data) {
        ModuleVersionBean moduleVersion = getModuleVersion(id, major, minor, revision);
        moduleVersion.setImage(data);
    }

    @Override
    public void removeModuleVersionImage(Long id, Integer major, Integer minor, Integer revision) {
        ModuleVersionBean moduleVersion = getModuleVersion(id, major, minor, revision);
        byte[] data = moduleVersion.getImage();
        moduleVersion.setImage(null);
    }

    @Override
    public ModuleVersionBean getModuleVersion(Long id) {
        return getModuleVersion(id, null, null, null);
    }


    @Override
    public ModuleVersionBean getModuleVersion(Long id, Integer major, Integer minor, Integer revision) {
        String strQuery = "SELECT x FROM ModuleVersion x WHERE x.module.id = :id";
        if (major != null && minor != null && revision != null)
            strQuery += " AND x.major = :major AND x.minor = :minor AND x.revision = :revision";
        else
            strQuery += " AND x.available = true ORDER by x.major DESC, x.minor DESC, x.revision DESC";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setMaxResults(1);
        q.setParameter("id", id);
        if (major != null && minor != null && revision != null) {
            q.setParameter("major", major);
            q.setParameter("minor", minor);
            q.setParameter("revision", revision);
        }

        return (ModuleVersionBean) q.getSingleResult();
    }

    @Override
    public Long setModuleVersion(ModuleVersion moduleVersion) {
        ModuleVersionBean moduleVersionBean;

        // Get or Create the entity
        if (moduleVersion.getModule().getId() != null) {
            // Create the PK
            ModuleVersionPK pk = new ModuleVersionPK();
            pk.setModule(getModule(moduleVersion.getModule().getId()));
            pk.setMajor(moduleVersion.getMajor());
            pk.setMinor(moduleVersion.getMinor());
            pk.setRevision(moduleVersion.getRevision());

            moduleVersionBean = entityManager.find(ModuleVersionBean.class, pk);
            if (moduleVersionBean == null) {
                moduleVersionBean = new ModuleVersionBean();
                moduleVersionBean.setModule(pk.getModule());
                moduleVersionBean.setMajor(pk.getMajor());
                moduleVersionBean.setMinor(pk.getMinor());
                moduleVersionBean.setRevision(pk.getRevision());
            }
        } else {
            ModuleBean module = new ModuleBean();
            entityManager.persist(module);

            moduleVersionBean = new ModuleVersionBean();
            moduleVersionBean.setModule(module);
            moduleVersionBean.setMajor(moduleVersion.getMajor());
            moduleVersionBean.setMinor(moduleVersion.getMinor());
            moduleVersionBean.setRevision(moduleVersion.getRevision());
        }

        moduleVersionBean.setAuthor(moduleVersion.getAuthor());
        moduleVersionBean.setDate(moduleVersion.getDate());
        moduleVersionBean.setDescription(moduleVersion.getDescription());
        moduleVersionBean.setName(moduleVersion.getName());
        moduleVersionBean.setUrl(moduleVersion.getUrl());

        return entityManager.merge(moduleVersionBean).getModule().getId();
    }

    @Override
    public void removeModuleVersion(Long id, Integer major, Integer minor, Integer revision) {
        ModuleVersionPK pk = new ModuleVersionPK();
        pk.setModule(entityManager.find(ModuleBean.class, id));
        pk.setMajor(major);
        pk.setMinor(minor);
        pk.setRevision(revision);

        ModuleVersionBean moduleVersion = entityManager.find(ModuleVersionBean.class, pk);
        entityManager.remove(moduleVersion);
    }

    @Override
    public Collection getModulesByName(String filter, String order, Integer itemByPage, Integer page) {
        String strQuery = "SELECT x FROM ModuleVersion x";
        if (filter != null) {
            strQuery += " WHERE x.name LIKE %:filter%";
        }
        if (order != null) {
            strQuery += " ORDER BY x.name";
        }

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        if (filter != null)
            q.setParameter("filter", filter);
        if (itemByPage != null) {
            q.setMaxResults(itemByPage);
            if (page != null) {
                q.setFirstResult(itemByPage * page);
            }
        }

        return q.getResultList();
    }


    ///////////////////////////////////////////////////////////////////////////
    /// Repository  Entity
    ///////////////////////////////////////////////////////////////////////////


    public Map getRepositoryEntries(Long moduleId) {
        return getRepositoryEntries(moduleId, null, null, null);
    }

    public Map getRepositoryEntries(Long moduleId, Integer major, Integer minor, Integer revision) {
        Map map = new HashMap();

        for (RepositoryEntityBean entityBean : getModuleVersion(moduleId, major, minor, revision).getRepositoryEntities()) {
            map.put(entityBean.getRepository(), entityBean.getKey());
        }

        return map;
    }

    public void setRepositoryEntry(Long repositoryId, Long id, Integer major, Integer minor, Integer revision, String key) {
        RepositoryEntityBean repositoryEntityBean;

        // Create the PK
        RepositoryEntityPK pk = new RepositoryEntityPK();
        pk.setRepository(getRepository(repositoryId));
        pk.setModuleVersion(getModuleVersion(id, major, minor, revision));

        // Get or Create the entity
        repositoryEntityBean = entityManager.find(RepositoryEntityBean.class, pk);
        if (repositoryEntityBean == null) {
            repositoryEntityBean = new RepositoryEntityBean();
            repositoryEntityBean.setModuleVersion(pk.getModuleVersion());
            repositoryEntityBean.setRepository(pk.getRepository());
        }

        // Set the fields
        repositoryEntityBean.setKey(key);

        entityManager.merge(repositoryEntityBean);
    }

    public void removeRepositoryEntry(Long repositoryId, Long moduleId, Integer major, Integer minor, Integer revision) {
        RepositoryEntityPK pk = new RepositoryEntityPK();
        pk.setModuleVersion(getModuleVersion(moduleId, major, minor, revision));
        pk.setRepository(getRepository(repositoryId));

        RepositoryEntityBean repositoryEntity = entityManager.find(RepositoryEntityBean.class, pk);
        entityManager.remove(repositoryEntity);
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Repository
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Collection getRepositoriesByType(String type) {
        String strQuery = "SELECT x FROM Repository x WHERE x.type = :type";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("type", type);

        return (Collection<RepositoryBean>) q.getResultList();
    }

    @Override
    public RepositoryBean getRepository(Long id) {
        String strQuery = "SELECT x FROM Repository x WHERE x.id = :id";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("id", id);

        return (RepositoryBean) q.getSingleResult();
    }

    @Override
    public Long setRepository(Repository repository) {
        RepositoryBean repositoryBean;

        // Get or Create the entity
        if (repository.getId() != null)
            repositoryBean = getRepository(repository.getId());
        else
            repositoryBean = new RepositoryBean();

        // Set the fields
        repositoryBean.setName(repository.getName());
        repositoryBean.setType(repository.getType());
        repositoryBean.setProperties(repository.getProperties());

        return entityManager.merge(repositoryBean).getId();
    }


    @Override
    public void removeRepository(Long id) {
        RepositoryBean repository = entityManager.find(RepositoryBean.class, id);
        entityManager.remove(repository);
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Category
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public CategoryBean getCategory(Long id) {
        String strQuery = "SELECT x FROM Category x WHERE x.id = :id";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("id", id);

        return (CategoryBean) q.getSingleResult();
    }

    @Override
    public Collection getCategories() {
        String strQuery = "SELECT x FROM Category x";

        // Create the query
        Query q = entityManager.createQuery(strQuery);

        return (Collection<CategoryBean>) q.getResultList();
    }

    @Override
    public Long setCategory(Category category) {
        CategoryBean categoryBean;

        // Get or Create the entity
        if (category.getId() != null)
            categoryBean = getCategory(category.getId());
        else
            categoryBean = new CategoryBean();

        categoryBean.setName(category.getName());
        categoryBean.setDescription(category.getDescription());
        entityManager.persist(categoryBean);

        return categoryBean.getId();
    }

    @Override
    public void removeCategory(Long id) {
        CategoryBean category = entityManager.find(CategoryBean.class, id);
        for (Iterator<ModuleBean> moduleIt = category.getModules().iterator(); moduleIt.hasNext(); ) {
            ModuleBean module = moduleIt.next();
            module.getCategories().remove(category);
            moduleIt.remove();
        }
        entityManager.remove(category);
    }

    @Override
    public void addModuleToCategory(Long categoryId, Long moduleId) {
        CategoryBean category = getCategory(categoryId);
        ModuleBean module = getModule(moduleId);

        category.getModules().add(module);
        module.getCategories().add(category);
    }

    @Override
    public void removeModuleFromCategory(Long categoryId, Long moduleId) {
        CategoryBean category = getCategory(categoryId);
        ModuleBean module = getModule(moduleId);

        category.getModules().remove(module);
        module.getCategories().remove(category);
    }
}
