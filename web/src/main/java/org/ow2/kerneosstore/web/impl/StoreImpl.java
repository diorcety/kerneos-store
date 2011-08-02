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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoreImpl implements IStore {

    /**
     * The logger.
     */
    private static Log logger = LogFactory.getLog(Activator.class);
    private static Pattern versionPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

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

    private String formatVersion(ModuleVersion moduleVersion) {
        return moduleVersion.getMajor() + "." +
                moduleVersion.getMinor() + "." +
                moduleVersion.getRevision();
    }

    private void setVersion(ModuleVersion moduleVersion, String version) {
        Matcher matcher = versionPattern.matcher(version);
        if (!matcher.find()) {
            throw new RuntimeException("Invalid version format");
        }
        moduleVersion.setMajor(Integer.parseInt(matcher.group(1)));
        moduleVersion.setMinor(Integer.parseInt(matcher.group(2)));
        moduleVersion.setRevision(Integer.parseInt(matcher.group(3)));
    }

    private Module EJB_2_API(ModuleVersion moduleVersion) {
        Module module = new Module();

        module.setId(moduleVersion.getModule().getId());
        module.setName(moduleVersion.getName());
        module.setVersion(formatVersion(moduleVersion));
        module.setDate(moduleVersion.getDate());
        module.setDescription(moduleVersion.getDescription());
        module.setImage(moduleVersion.getImage());

        return module;
    }

    private ModuleVersion API_2_EJB(Module module) {
        ModuleVersion moduleVersion = new ModuleVersion();

        if (module.getId() != null)
            moduleVersion.setModule(store.getModule(module.getId()));
        moduleVersion.setName(module.getName());
        setVersion(moduleVersion, module.getVersion());
        moduleVersion.setDate(module.getDate());
        moduleVersion.setDescription(module.getDescription());
        moduleVersion.setImage(module.getImage());

        return moduleVersion;
    }

    @Override
    public Modules getModules(String filter, String order, String fieldName, Integer itemByPage, Integer page) {
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
                moduleArray[i] = EJB_2_API(iterator.next());
                i++;
            }
            return new Modules(moduleArray);
        }

        return new Modules(new Module[0]);
    }

    @Override
    public Module setModule(Module module) {
        ModuleVersion moduleVersion = store.setModule(API_2_EJB(module));
        return EJB_2_API(moduleVersion);
    }

    @Override
    public Modules getUpdatedModules(Modules modules) {
        return null;
    }

    @Override
    public Module getModule(Long id, Integer major, Integer minor, Integer revision) {
        ModuleVersion moduleVersion = store.getModule(id, major, minor, revision);
        return EJB_2_API(moduleVersion);
    }


    @Override
    public Module enableModule(Long id, Integer major, Integer minor, Integer revision) {
        ModuleVersion moduleVersion = store.getModule(id, major, minor, revision);
        moduleVersion.setAvailable(true);
        moduleVersion = store.setModule(moduleVersion);
        return EJB_2_API(moduleVersion);
    }

    @Override
    public Module disableModule(Long id, Integer major, Integer minor, Integer revision) {
        ModuleVersion moduleVersion = store.getModule(id, major, minor, revision);
        moduleVersion.setAvailable(false);
        moduleVersion = store.setModule(moduleVersion);
        return EJB_2_API(moduleVersion);
    }

    @Override
    public Byte[] downloadModule(Long id) {
        return new Byte[0];
    }
}
