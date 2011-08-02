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


import org.ow2.kerneosstore.core.Module;
import org.ow2.kerneosstore.core.ModuleVersion;
import org.ow2.kerneosstore.core.ModuleVersionPK;
import org.ow2.kerneosstore.core.Repository;
import org.ow2.kerneosstore.core.RepositoryEntity;
import org.ow2.kerneosstore.core.RepositoryEntityPK;
import org.ow2.kerneosstore.core.Store;
import org.ow2.kerneosstore.core.StoreInfo;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;

@Stateless
public class StoreLocal implements Store {

    /**
     * Entity manager used by this session bean.
     */
    @PersistenceContext
    private EntityManager entityManager = null;

    ///////////////////////////////////////////////////////////////////////////
    /// Store
    ///////////////////////////////////////////////////////////////////////////

    public StoreInfo getStoreInfo() {
        return (StoreInfo) entityManager.createNamedQuery("store.info").getSingleResult();
    }

    @Override
    public StoreInfo setStoreInfo(StoreInfo storeInfo) {
        return entityManager.merge(storeInfo);
    }


    ///////////////////////////////////////////////////////////////////////////
    /// Module
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Module getModule(Long id) {
        String strQuery = "SELECT x FROM Module x WHERE x.id = :id";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("id", id);

        Module module = (Module) q.getSingleResult();
        // Force EAGER
        module.getVersions().size();
        module.getCategories().size();
        return module;
    }

    @Override
    public Module setModule(Module module) {
        return entityManager.merge(module);
    }

    @Override
    public Module removeModule(Long id) {
        Module module = entityManager.find(Module.class, id);
        entityManager.remove(module);
        return module;
    }


    ///////////////////////////////////////////////////////////////////////////
    /// ModuleVersion
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public ModuleVersion getModuleVersion(Long id, Integer major, Integer minor, Integer revision) {
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

        return (ModuleVersion) q.getSingleResult();
    }

    @Override
    public ModuleVersion setModuleVersion(ModuleVersion moduleVersion) {
        return entityManager.merge(moduleVersion);
    }

    @Override
    public ModuleVersion removeModuleVersion(Long id, Integer major, Integer minor, Integer revision) {
        ModuleVersionPK pk = new ModuleVersionPK();
        pk.setModule(entityManager.find(Module.class, id));
        pk.setMajor(major);
        pk.setMinor(minor);
        pk.setRevision(revision);

        ModuleVersion moduleVersion = entityManager.find(ModuleVersion.class, pk);
        entityManager.remove(moduleVersion);
        return moduleVersion;
    }

    @Override
    public Collection<ModuleVersion> getModulesByName(String filter, String order, Integer itemByPage, Integer page) {
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

        return (Collection<ModuleVersion>) q.getResultList();
    }


    ///////////////////////////////////////////////////////////////////////////
    /// Repository
    ///////////////////////////////////////////////////////////////////////////

    public Collection<RepositoryEntity> getRepositoryEntities(Long moduleId, Integer major, Integer minor, Integer revision) {
        Collection<RepositoryEntity> repositoryEntities = getModuleVersion(moduleId, major, minor, revision).getRepositories();

        // Force EAGER
        repositoryEntities.size();

        return repositoryEntities;
    }

    public RepositoryEntity setRepositoryEntity(RepositoryEntity repositoryEntity) {
        return entityManager.merge(repositoryEntity);
    }

    public RepositoryEntity removeRepositoryEntity(Long repositoryId, Long moduleId, Integer major, Integer minor, Integer revision) {
        RepositoryEntityPK pk = new RepositoryEntityPK();
        pk.setModuleVersion(getModuleVersion(moduleId, major, minor, revision));
        pk.setRepository(getRepository(repositoryId));

        RepositoryEntity repositoryEntity = entityManager.find(RepositoryEntity.class, pk);
        entityManager.remove(repositoryEntity);
        return repositoryEntity;
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Repository
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Collection<Repository> getRepositoriesByType(String type) {
        String strQuery = "SELECT x FROM Repository x WHERE x.type = :type";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("type", type);

        return (Collection<Repository>) q.getResultList();
    }

    @Override
    public Repository getRepository(Long id) {
        String strQuery = "SELECT x FROM Repository x WHERE x.id = :id";

        // Create the query
        Query q = entityManager.createQuery(strQuery);
        q.setParameter("id", id);

        return (Repository) q.getSingleResult();
    }

    @Override
    public Repository setRepository(Repository repository) {
        return entityManager.merge(repository);
    }


    @Override
    public Repository removeRepository(Long id) {
        Repository repository = entityManager.find(Repository.class, id);
        entityManager.remove(repository);
        return repository;
    }
}
