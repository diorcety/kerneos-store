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

import org.ow2.kerneosstore.api.Module;
import org.ow2.kerneosstore.api.Modules;
import org.ow2.kerneosstore.api.IStore;
import org.ow2.kerneosstore.api.StoreInfo;
import org.ow2.kerneosstore.core.ModuleVersion;
import org.ow2.kerneosstore.core.Store;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import java.util.Collection;
import java.util.Iterator;

public class StoreImpl implements IStore {
    /**
     * The logger.
     */
    private static Log logger = LogFactory.getLog(Activator.class);

    private Store store;

    public StoreImpl(Store store) {
        this.store = store;
    }

    @Override
    public StoreInfo getInfo() {
        try {
            org.ow2.kerneosstore.core.StoreInfo strI = store.getStoreInfo();
            StoreInfo storeInfo = new StoreInfo();

            // Copy information
            storeInfo.setName(strI.getName());
            storeInfo.setDescription(strI.getDescription());
            storeInfo.setUrl(strI.getUrl());

            return storeInfo;
        } catch (Exception e) {
            logger.error("Store not initialized");
        }
        return null;
    }

    @Override
    public void setInfo(StoreInfo storeInfo) {
        org.ow2.kerneosstore.core.StoreInfo strI = new org.ow2.kerneosstore.core.StoreInfo();

        // Copy information
        strI.setName(storeInfo.getName());
        strI.setDescription(storeInfo.getDescription());
        strI.setUrl(storeInfo.getUrl());

        store.setStoreInfo(strI);
    }

    @Override
    public Modules getModules(String filter, String order, String fieldName, Integer itemByPage, Integer page) {
        try {
            Collection<ModuleVersion> modules = null;
            if (fieldName == null) {
                modules = store.getModulesByName(filter, order, itemByPage, page);
            } else if (fieldName.equals("name")) {
                modules = store.getModulesByName(filter, order, itemByPage, page);
            }

            // Transform to array
            if (modules != null) {
                Module[] moduleArray = new Module[modules.size()];
                int i = 0;
                for (Iterator<ModuleVersion> iterator = modules.iterator(); iterator.hasNext(); ) {
                    ModuleVersion moduleVersion = iterator.next();
                    i++;

                    // Copy information
                    moduleArray[i] = new Module();
                    moduleArray[i].setId(moduleVersion.getModule().getId());
                    moduleArray[i].setName(moduleVersion.getName());
                    moduleArray[i].setVersion(moduleVersion.getVersion());
                    moduleArray[i].setDescription(moduleVersion.getDescription());
                    moduleArray[i].setImage(moduleVersion.getImage());
                }
                return new Modules(moduleArray);
            }
        } catch (Exception e) {
            logger.error("Can't get the modules");
        }

        return null;
    }

    @Override
    public Modules getUpdatedModules(Modules modules) {
        return null;
    }

    @Override
    public Byte[] downloadModule(Module module) {
        return new Byte[0];
    }
}
