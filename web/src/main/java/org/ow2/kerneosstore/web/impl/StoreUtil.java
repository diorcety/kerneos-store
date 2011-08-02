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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoreUtil {
    private static Pattern versionPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

    ///////////////////////////////////////////////////////////////////////////
    /// Store
    ///////////////////////////////////////////////////////////////////////////

    public static org.ow2.kerneosstore.core.StoreInfo API_2_EJB(org.ow2.kerneosstore.api.StoreInfo storeInfo) {
        org.ow2.kerneosstore.core.StoreInfo storeInfo2 = new org.ow2.kerneosstore.core.StoreInfo();

        storeInfo2.setName(storeInfo.getName());
        storeInfo2.setDescription(storeInfo.getDescription());
        storeInfo2.setUrl(storeInfo.getUrl());

        return storeInfo2;
    }

    public static org.ow2.kerneosstore.api.StoreInfo EJB_2_API(org.ow2.kerneosstore.core.StoreInfo storeInfo) {
        org.ow2.kerneosstore.api.StoreInfo storeInfo2 = new org.ow2.kerneosstore.api.StoreInfo();

        storeInfo2.setName(storeInfo.getName());
        storeInfo2.setDescription(storeInfo.getDescription());
        storeInfo2.setUrl(storeInfo.getUrl());

        return storeInfo2;
    }


    ///////////////////////////////////////////////////////////////////////////
    /// Module
    ///////////////////////////////////////////////////////////////////////////

    private static String formatVersion(org.ow2.kerneosstore.core.ModuleVersion moduleVersion) {
        return moduleVersion.getMajor() + "." +
                moduleVersion.getMinor() + "." +
                moduleVersion.getRevision();
    }

    private static void setVersion(org.ow2.kerneosstore.core.ModuleVersion moduleVersion, String version) {
        Matcher matcher = versionPattern.matcher(version);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid version format");
        }
        moduleVersion.setMajor(Integer.parseInt(matcher.group(1)));
        moduleVersion.setMinor(Integer.parseInt(matcher.group(2)));
        moduleVersion.setRevision(Integer.parseInt(matcher.group(3)));
    }

    public static org.ow2.kerneosstore.api.Module EJB_2_API(org.ow2.kerneosstore.core.ModuleVersion moduleVersion) {
        org.ow2.kerneosstore.api.Module module = new org.ow2.kerneosstore.api.Module();

        module.setId(moduleVersion.getModule().getId());
        module.setName(moduleVersion.getName());
        module.setVersion(formatVersion(moduleVersion));
        module.setDate(moduleVersion.getDate());
        module.setDescription(moduleVersion.getDescription());
        module.setAuthor(moduleVersion.getAuthor());
        module.setUrl(moduleVersion.getUrl());

        return module;
    }

    public static org.ow2.kerneosstore.core.ModuleVersion API_2_EJB(org.ow2.kerneosstore.api.Module module) {
        org.ow2.kerneosstore.core.ModuleVersion moduleVersion = new org.ow2.kerneosstore.core.ModuleVersion();
        moduleVersion.setName(module.getName());
        setVersion(moduleVersion, module.getVersion());
        moduleVersion.setDate(module.getDate());
        moduleVersion.setDescription(module.getDescription());
        moduleVersion.setAuthor(module.getAuthor());
        moduleVersion.setUrl(module.getUrl());

        return moduleVersion;
    }

    ///////////////////////////////////////////////////////////////////////////
    /// Repository
    ///////////////////////////////////////////////////////////////////////////

    public static org.ow2.kerneosstore.core.Repository API_2_EJB(org.ow2.kerneosstore.api.Repository repository) {
        org.ow2.kerneosstore.core.Repository repository2 = new org.ow2.kerneosstore.core.Repository();

        repository2.setId(repository.getId());
        repository2.setName(repository.getName());
        repository2.setType(repository.getType());
        repository2.getProperties().putAll(repository.getProperties());

        return repository2;
    }

    public static org.ow2.kerneosstore.api.Repository EJB_2_API(org.ow2.kerneosstore.core.Repository repository) {
        org.ow2.kerneosstore.api.Repository repository2 = new org.ow2.kerneosstore.api.Repository();

        repository2.setId(repository.getId());
        repository2.setName(repository.getName());
        repository2.setType(repository.getType());
        repository2.getProperties().putAll(repository.getProperties());

        return repository2;
    }
}
