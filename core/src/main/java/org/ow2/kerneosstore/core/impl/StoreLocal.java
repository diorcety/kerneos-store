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

import org.ow2.kerneosstore.api.CategoryMeta;
import org.ow2.kerneosstore.api.ModuleMeta;
import org.ow2.kerneosstore.api.ModuleVersion;
import org.ow2.kerneosstore.api.Repository;
import org.ow2.kerneosstore.api.RepositoryMeta;
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

    public ModuleBean getModule(String id) {
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
    public Collection<ModuleVersionBean> getModules(String id) {
        String strQuery = "SELECT x FROM Module x WHERE x.id = :id";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("id", id);

        ModuleBean module = (ModuleBean) q.getSingleResult();

        // Force EAGER
        module.getVersions().size();
        return (Collection<ModuleVersionBean>) module.getVersions();
    }

    @Override
    public void removeModule(String id) {
        ModuleBean module = entityManager.find(ModuleBean.class, id);
        entityManager.remove(module);
    }


    ///////////////////////////////////////////////////////////////////////////
    /// ModuleVersion
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void enableModuleVersion(String id, Integer major, Integer minor, Integer revision) {
        ModuleVersionBean moduleVersion = getModuleVersion(id, major, minor, revision);
        moduleVersion.setAvailable(true);
    }

    @Override
    public void disableModuleVersion(String id, Integer major, Integer minor, Integer revision) {
        ModuleVersionBean moduleVersion = getModuleVersion(id, major, minor, revision);
        moduleVersion.setAvailable(false);
    }

    @Override
    public byte[] getModuleVersionImage(String id) {
        return getModuleVersionImage(id, null, null, null);
    }

    @Override
    public byte[] getModuleVersionImage(String id, Integer major, Integer minor, Integer revision) {
        return getModuleVersion(id, major, minor, revision).getImage();
    }

    @Override
    public void setModuleVersionImage(String id, Integer major, Integer minor, Integer revision, byte[] data) {
        ModuleVersionBean moduleVersion = getModuleVersion(id, major, minor, revision);
        moduleVersion.setImage(data);
    }

    @Override
    public void removeModuleVersionImage(String id, Integer major, Integer minor, Integer revision) {
        ModuleVersionBean moduleVersion = getModuleVersion(id, major, minor, revision);
        byte[] data = moduleVersion.getImage();
        moduleVersion.setImage(null);
    }

    @Override
    public ModuleVersionBean getModuleVersion(String id) {
        return getModuleVersion(id, null, null, null);
    }


    @Override
    public ModuleVersionBean getModuleVersion(String id, Integer major, Integer minor, Integer revision) {
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
    public void setModuleVersion(String id, Integer major, Integer minor, Integer revision, ModuleMeta moduleMeta) {
        ModuleVersionBean moduleVersionBean;
        ModuleBean moduleBean = entityManager.find(ModuleBean.class, id);

        // Get or Create the entity
        if (moduleBean != null) {
            // Create the PK
            ModuleVersionPK pk = new ModuleVersionPK();
            pk.setModule(moduleBean);
            pk.setMajor(major);
            pk.setMinor(minor);
            pk.setRevision(revision);

            moduleVersionBean = entityManager.find(ModuleVersionBean.class, pk);
            if (moduleVersionBean == null) {
                moduleVersionBean = new ModuleVersionBean();
                moduleVersionBean.setModule(pk.getModule());
                moduleVersionBean.setMajor(pk.getMajor());
                moduleVersionBean.setMinor(pk.getMinor());
                moduleVersionBean.setRevision(pk.getRevision());
            }
        } else {
            moduleBean = new ModuleBean();
            moduleBean.setId(id);
            entityManager.persist(moduleBean);

            moduleVersionBean = new ModuleVersionBean();
            moduleVersionBean.setModule(moduleBean);
            moduleVersionBean.setMajor(major);
            moduleVersionBean.setMinor(minor);
            moduleVersionBean.setRevision(revision);
        }

        moduleVersionBean.setAuthor(moduleMeta.getAuthor());
        moduleVersionBean.setDate(moduleMeta.getDate());
        moduleVersionBean.setDescription(moduleMeta.getDescription());
        moduleVersionBean.setName(moduleMeta.getName());
        moduleVersionBean.setUrl(moduleMeta.getUrl());

        entityManager.merge(moduleVersionBean);
    }

    @Override
    public void removeModuleVersion(String id, Integer major, Integer minor, Integer revision) {
        ModuleVersionPK pk = new ModuleVersionPK();
        pk.setModule(entityManager.find(ModuleBean.class, id));
        pk.setMajor(major);
        pk.setMinor(minor);
        pk.setRevision(revision);

        ModuleVersionBean moduleVersion = entityManager.find(ModuleVersionBean.class, pk);
        entityManager.remove(moduleVersion);
    }

    @Override
    public Collection<ModuleVersionBean> searchModules(String filter, String field, String order, Integer itemByPage, Integer page) {
        boolean strictFilter = false;
        String strQuery = "SELECT x FROM ModuleVersion x WHERE x.available = true AND (" +
                "SELECT count(*) FROM ModuleVersion z WHERE z.module.id = x.module.id AND z.available = true AND z.major > x.major) = 0 AND (" +
                "SELECT count(*) FROM ModuleVersion z WHERE z.module.id = x.module.id AND z.available = true AND z.major = x.major AND z.minor > x.minor) = 0 AND (" +
                "SELECT count(*) FROM ModuleVersion z WHERE z.module.id = x.module.id AND z.available = true AND z.major = x.major AND z.minor = x.minor AND z.revision > x.revision ) = 0 AND";

        if ("author".equalsIgnoreCase(field)) {

            // Author
            strQuery += " LOWER(x.author) LIKE :filter";
            if (order != null) {
                if ("desc".equals(order))
                    strQuery += " ORDER BY x.author DESC";
                else
                    strQuery += " ORDER BY x.author ASC";
            }
        } else if ("description".equalsIgnoreCase(field)) {

            // Description
            strQuery += " LOWER(x.description) LIKE :filter";
            if (order != null) {
                if ("desc".equals(order))
                    strQuery += " ORDER BY x.description DESC";
                else
                    strQuery += " ORDER BY x.description ASC";
            }
        } else {

            // Name (default)
            strQuery += " LOWER(x.name) LIKE :filter";
            if (order != null) {
                if ("desc".equals(order))
                    strQuery += " ORDER BY x.name DESC";
                else
                    strQuery += " ORDER BY x.name ASC";
            }
        }

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        if (filter != null)
            if (strictFilter)
                q.setParameter("filter", filter.toLowerCase());
            else
                q.setParameter("filter", "%" + filter.toLowerCase() + "%");
        if (itemByPage != null) {
            q.setMaxResults(itemByPage);
            if (page != null) {
                q.setFirstResult(itemByPage * page);
            }
        }

        return (Collection<ModuleVersionBean>) q.getResultList();
    }


    ///////////////////////////////////////////////////////////////////////////
    /// Repository  Entity
    ///////////////////////////////////////////////////////////////////////////


    public Map getRepositoryEntries(String moduleId) {
        return getRepositoryEntries(moduleId, null, null, null);
    }

    public Map getRepositoryEntries(String moduleId, Integer major, Integer minor, Integer revision) {
        Map map = new HashMap();

        for (RepositoryEntityBean entityBean : getModuleVersion(moduleId, major, minor, revision).getRepositoryEntities()) {
            map.put(entityBean.getRepository(), entityBean.getKey());
        }

        return map;
    }

    public void setRepositoryEntry(Long repositoryId, String id, Integer major, Integer minor, Integer revision, String key) {
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

    public void removeRepositoryEntry(Long repositoryId, String moduleId, Integer major, Integer minor, Integer revision) {
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
    public Collection<RepositoryBean> getRepositoriesByType(String type) {
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
    public void setRepository(Long id, RepositoryMeta repositoryMeta) {
        RepositoryBean repositoryBean = entityManager.find(RepositoryBean.class, id);

        // Get or Create the entity
        if (repositoryBean == null) {
            repositoryBean = new RepositoryBean();
            repositoryBean.setId(id);
        }

        // Set the fields
        repositoryBean.setName(repositoryMeta.getName());
        repositoryBean.setType(repositoryMeta.getType());
        repositoryBean.setProperties(repositoryMeta.getProperties());

        entityManager.merge(repositoryBean);
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
    public CategoryBean getCategory(String id) {
        String strQuery = "SELECT x FROM Category x WHERE x.id = :id";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("id", id);

        return (CategoryBean) q.getSingleResult();
    }

    @Override
    public Collection<CategoryBean> getCategories() {
        String strQuery = "SELECT x FROM Category x";

        // Create the query
        Query q = entityManager.createQuery(strQuery);

        return (Collection<CategoryBean>) q.getResultList();
    }

    @Override
    public void setCategory(String id, CategoryMeta categoryMeta) {
        CategoryBean categoryBean = entityManager.find(CategoryBean.class, id);

        // Get or Create the entity
        if (categoryBean == null) {
            categoryBean = new CategoryBean();
            categoryBean.setId(id);
        }

        categoryBean.setName(categoryMeta.getName());
        categoryBean.setDescription(categoryMeta.getDescription());
        entityManager.merge(categoryBean);
    }

    @Override
    public void removeCategory(String id) {
        CategoryBean category = entityManager.find(CategoryBean.class, id);
        for (Iterator<ModuleBean> moduleIt = category.getModules().iterator(); moduleIt.hasNext(); ) {
            ModuleBean module = moduleIt.next();
            module.getCategories().remove(category);
            moduleIt.remove();
        }
        entityManager.remove(category);
    }

    @Override
    public void addModuleToCategory(String categoryId, String moduleId) {
        CategoryBean category = getCategory(categoryId);
        ModuleBean module = getModule(moduleId);

        if (!category.getModules().contains(module)) {
            category.getModules().add(module);
            module.getCategories().add(category);
        }
    }

    @Override
    public void removeModuleFromCategory(String categoryId, String moduleId) {
        CategoryBean category = getCategory(categoryId);
        ModuleBean module = getModule(moduleId);

        if (category.getModules().contains(module)) {
            category.getModules().remove(module);
            module.getCategories().remove(category);
        }
    }

    @Override
    public Collection<? extends ModuleVersion> searchModulesByCategory(String id, String field, String order, Integer itemByPage, Integer page) {
        //TODO id to lower case
        String strQuery = "SELECT x FROM Category y, ModuleVersion x, IN(y.modules) z WHERE y.id=:id AND z.id = x.module.id AND x.available = true AND (" +
                "SELECT count(*) FROM ModuleVersion z WHERE z.module.id = x.module.id AND z.available = true AND z.major > x.major) = 0 AND (" +
                "SELECT count(*) FROM ModuleVersion z WHERE z.module.id = x.module.id AND z.available = true AND z.major = x.major AND z.minor > x.minor) = 0 AND (" +
                "SELECT count(*) FROM ModuleVersion z WHERE z.module.id = x.module.id AND z.available = true AND z.major = x.major AND z.minor = x.minor AND z.revision > x.revision ) = 0";
        if ("author".equalsIgnoreCase(field)) {

            // Author
            if (order != null) {
                if ("desc".equals(order))
                    strQuery += " ORDER BY x.author DESC";
                else
                    strQuery += " ORDER BY x.author ASC";
            }
        } else if ("description".equalsIgnoreCase(field)) {

            // Description
            if (order != null) {
                if ("desc".equals(order))
                    strQuery += " ORDER BY x.description DESC";
                else
                    strQuery += " ORDER BY x.description ASC";
            }
        } else {

            // Name (default)
            if (order != null) {
                if ("desc".equals(order))
                    strQuery += " ORDER BY x.name DESC";
                else
                    strQuery += " ORDER BY x.name ASC";
            }
        }
        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("id", id);
        if (itemByPage != null) {
            q.setMaxResults(itemByPage);
            if (page != null) {
                q.setFirstResult(itemByPage * page);
            }
        }

        return (Collection<ModuleVersionBean>) q.getResultList();
    }
}
