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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface IStoreClient {

    ///////////////////////////////////////////////////////////////////////////
    /// Store
    ///////////////////////////////////////////////////////////////////////////

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_XML)
    public StoreInfo getInfo();


    ///////////////////////////////////////////////////////////////////////////
    /// Module
    ///////////////////////////////////////////////////////////////////////////

    @GET
    @Path("/module/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Module getModule(@PathParam("id") Long id);

    @GET
    @Path("/module/{id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] downloadModule(@PathParam("id") Long id);

    @GET
    @Path("/module/{id}/image")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] getModuleImage(@PathParam("id") Long id);

    @GET
    @Path("/modules")
    @Produces(MediaType.APPLICATION_XML)
    public Modules getModules(
            @QueryParam("filter") String filter,
            @QueryParam("order") String order,
            @QueryParam("fieldName") String fieldName,
            @QueryParam("itemByPage") Integer itemByPage,
            @QueryParam("page") Integer page);

    @GET
    @Path("/modules/latest")
    public Modules getUpdatedModules(Modules modules);
}