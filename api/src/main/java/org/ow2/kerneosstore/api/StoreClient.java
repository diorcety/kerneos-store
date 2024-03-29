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

public interface StoreClient {

    // Store

    public Store getStore();

    // RepositoryEntry

    public Map<Repository, String> getRepositoryEntries(String moduleId);

    // ModuleVersion

    public byte[] getModuleVersionImage(String id);

    public ModuleVersion getModuleVersion(String id);

    public Collection<? extends ModuleVersion> searchModules(String filter, String field, String order, Integer itemByPage, Integer page);

    public ModuleIdsWrapper searchModulesGetIds(String filter);

    public String searchModulesResultsNumber(String filter);

    public Collection<? extends ModuleVersion> searchModulesByCategory(String id, String field, String order, Integer itemByPage, Integer page);

    public ModuleIdsWrapper searchModulesByCategoryGetIds(String id);

    public String searchModulesByCategoryResultsNumber(String id);

    // Category

    public Collection<? extends Category> getCategories();

    public Category getCategory(String id);

}
