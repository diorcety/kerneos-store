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
import org.ow2.kerneosstore.core.Store;
import org.ow2.kerneosstore.core.StoreInfo;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
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

    public StoreInfo getStoreInfo() {
        return (StoreInfo) entityManager.createNamedQuery("store.info").getSingleResult();
    }

    @Override
    public void setStoreInfo(StoreInfo storeInfo) {
        storeInfo = entityManager.merge(storeInfo);
    }

    public Collection<ModuleVersion> getModulesByName(String filter, String order, Integer itemByPage, Integer page) {
        String strQuery = "SELECT x FROM ModuleVersion x";
        if (filter != null) {
            strQuery += " WHERE x.module.name LIKE %:filter%";
        }
        if (order != null) {
            strQuery += " ORDER BY x.module.name";
        }
        Query q = entityManager.createQuery(strQuery);
        if (itemByPage != null) {
            q.setMaxResults(itemByPage);
            if (page != null) {
                q.setFirstResult(itemByPage * page);
            }
        }

        return q.getResultList();
    }
}
