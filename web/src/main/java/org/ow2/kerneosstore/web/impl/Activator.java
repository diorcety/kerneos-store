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

import org.ow2.kerneosstore.web.JerseyApplication;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.servlet.ServletException;
import java.util.Dictionary;
import java.util.Hashtable;

@Component
@Instantiate
public class Activator {
    /**
     * The logger.
     */
    private static Log logger = LogFactory.getLog(Activator.class);

    @Requires
    private HttpService httpService;

    @Property(name = "context", value = "/store")
    private String context;

    @Property(name = "name", value = "KStore")
    private String name;

    @Property(name = "description", value = "Official Kerneos Store")
    private String description;

    @Property(name = "url", value = "http://kerneos.ow2.org")
    private String url;

    @Validate
    private synchronized void start() throws ServletException, NamespaceException {
        logger.debug("Start Store WEB-RS");

        StoreInfo storeInfo = new StoreInfo(name, description, url);
        httpService.registerServlet(context, new ServletContainer(new JerseyApplication(storeInfo)), null, null);
    }

    @Invalidate
    private synchronized void stop() {
        logger.debug("Stop Store WEB-RS");
        httpService.unregister(context);
    }


}
