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

import org.ow2.kerneosstore.api.IModule;
import org.ow2.kerneosstore.api.IStore;

import java.util.Collection;

public class StoreImpl implements IStore {
    private StoreInfo storeInfo;

    public StoreImpl(StoreInfo storeInfo) {
        this.storeInfo = storeInfo;
    }

    @Override
    public StoreInfo getInfo() {
        return storeInfo;
    }

    @Override
    public IModule[] getModules(String fieldName, String filter, String order, int itemByPage, int page) {
        return null;
    }

    @Override
    public IModule[] getUpdatedModules(IModule[] modules) {
        return null;
    }

    @Override
    public Byte[] downloadModule(IModule module) {
        return new Byte[0];
    }
}
