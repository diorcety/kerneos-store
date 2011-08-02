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

package org.ow2.kerneosstore.core;

import javax.ejb.Local;
import java.util.Collection;

@Local
public interface Store {
    public StoreInfo getStoreInfo();

    public StoreInfo setStoreInfo(StoreInfo storeInfo);

    //

    public Module getModule(Long id);

    public Module setModule(Module module);

    public Module removeModule(Long id);

    //

    public ModuleVersion getModuleVersion(Long id, Integer major, Integer minor, Integer revision);

    public ModuleVersion setModuleVersion(ModuleVersion moduleVersion);

    public ModuleVersion removeModuleVersion(Long id, Integer major, Integer minor, Integer revision);

    public Collection<ModuleVersion> getModulesByName(String filter, String order, Integer itemByPage, Integer page);

    //

    public Collection<RepositoryEntity> getRepositoryEntities(Long moduleId, Integer major, Integer minor, Integer revision);

    public RepositoryEntity setRepositoryEntity(RepositoryEntity repositoryEntity);

    public RepositoryEntity removeRepositoryEntity(Long repositoryId, Long moduleId, Integer major, Integer minor, Integer revision);

    //

    public Collection<Repository> getRepositoriesByType(String type);

    public Repository getRepository(Long id);

    public Repository setRepository(Repository repository);

    public Repository removeRepository(Long id);
}
