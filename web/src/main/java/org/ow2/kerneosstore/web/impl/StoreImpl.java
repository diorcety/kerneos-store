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

import com.sun.jersey.spi.container.servlet.ServletContainer;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import org.ow2.kerneosstore.web.RSStoreAdmin;
import org.ow2.kerneosstore.web.RSStoreClient;
import org.ow2.kerneosstore.web.impl.provider.CategoryReader;
import org.ow2.kerneosstore.web.impl.provider.ModuleReader;
import org.ow2.kerneosstore.web.impl.provider.MyXMLListElementProvider;
import org.ow2.kerneosstore.web.impl.provider.RepositoryReader;
import org.ow2.kerneosstore.web.impl.provider.StoreReader;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@Component
@Instantiate
public class StoreImpl extends Application {
    /**
     * The logger.
     */
    private static Log logger = LogFactory.getLog(StoreImpl.class);


    @Property(name = "context", value = "/store")
    private String context;

    @Requires
    private HttpService httpService;

    @Requires(proxy = false)
    private RSStoreAdmin storeAdmin;

    @Requires(proxy = false)
    private RSStoreClient storeClient;

    private StoreImpl() {

    }

    ///////////////////////////////////////////////////////////////////////////
    /// Component life-cycle
    ///////////////////////////////////////////////////////////////////////////

    @Validate
    private void start() throws ServletException, NamespaceException {
        logger.debug("Start Store WEB-RS: " + context);

        httpService.registerServlet(context, new ServletContainer(this), null, null);
    }

    @Invalidate
    private void stop() {
        logger.debug("Stop Store WEB-RS: " + context);
        httpService.unregister(context);
    }


    ///////////////////////////////////////////////////////////////////////////
    /// JAXRS Application
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> objects = new HashSet<Class<?>>();
        objects.add(MyXMLListElementProvider.class);
        objects.add(EJBExceptionMapper.class);
        objects.add(CategoryReader.class);
        objects.add(ModuleReader.class);
        objects.add(RepositoryReader.class);
        objects.add(StoreReader.class);
        return objects;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = new HashSet<Object>();
        objects.add(storeAdmin);
        objects.add(storeClient);
        return objects;
    }

}
