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

package org.ow2.kerneosstore.api;

import java.util.Collection;
import java.util.Map;

public interface StoreAdmin {

    // Store

    public void setStore(Store store);

    // Module

    public Collection<ModuleVersion> getModules(Long id);

    public void removeModule(Long id);

    // ModuleVersion

    public void enableModuleVersion(Long id, Integer major, Integer minor, Integer revision);

    public void disableModuleVersion(Long id, Integer major, Integer minor, Integer revision);

    public byte[] getModuleVersionImage(Long id, Integer major, Integer minor, Integer revision);

    public void setModuleVersionImage(Long id, Integer major, Integer minor, Integer revision, byte[] data);

    public void removeModuleVersionImage(Long id, Integer major, Integer minor, Integer revision);

    public ModuleVersion getModuleVersion(Long id, Integer major, Integer minor, Integer revision);

    public Long setModuleVersion(ModuleVersion moduleVersion);

    public void removeModuleVersion(Long id, Integer major, Integer minor, Integer revision);

    // RepositoryEntry

    public Map<Repository, String> getRepositoryEntries(Long moduleId, Integer major, Integer minor, Integer revision);

    public void setRepositoryEntry(Long repositoryId, Long id, Integer major, Integer minor, Integer revision, String key);

    public void removeRepositoryEntry(Long repositoryId, Long moduleId, Integer major, Integer minor, Integer revision);

    // Repository

    public Collection<Repository> getRepositoriesByType(String type);

    public Repository getRepository(Long id);

    public Long setRepository(Repository repository);

    public void removeRepository(Long id);


    // Category

    public Long setCategory(Category category);

    public void removeCategory(Long id);

    public void addModuleToCategory(Long categoryId, Long moduleId);

    public void removeModuleFromCategory(Long categoryId, Long moduleId);

}
