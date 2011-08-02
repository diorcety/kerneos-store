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

import java.io.Serializable;

public class RepositoryEntityPK implements Serializable {

    private ModuleVersion moduleVersion;

    private Repository repository;

    public ModuleVersion getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(ModuleVersion moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RepositoryEntityPK that = (RepositoryEntityPK) o;

        if (moduleVersion != null ? !moduleVersion.equals(that.moduleVersion) : that.moduleVersion != null)
            return false;
        if (repository != null ? !repository.equals(that.repository) : that.repository != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = moduleVersion != null ? moduleVersion.hashCode() : 0;
        result = 31 * result + (repository != null ? repository.hashCode() : 0);
        return result;
    }
}
