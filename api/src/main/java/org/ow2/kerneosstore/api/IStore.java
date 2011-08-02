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
public interface IStore {
    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_XML)
    public StoreInfo getInfo();

    @POST
    @Path("/info")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public void setInfo(StoreInfo storeInfo);

    @POST
    @Path("/module")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Module setModule(Module module);

    @GET
    @Path("/module/{id}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Module getModule(@PathParam("id") Long id,
                            @QueryParam("major") Integer major,
                            @QueryParam("minor") Integer minor,
                            @QueryParam("revision") Integer revision);


    @GET
    @Path("/module/{id}/enable")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Module enableModule(@PathParam("id") Long id,
                               @QueryParam("major") Integer major,
                               @QueryParam("minor") Integer minor,
                               @QueryParam("revision") Integer revision);


    @GET
    @Path("/module/{id}/disable")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Module disableModule(@PathParam("id") Long id,
                                @QueryParam("major") Integer major,
                                @QueryParam("minor") Integer minor,
                                @QueryParam("revision") Integer revision);

    @GET
    @Path("/module/{id}/download")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Byte[] downloadModule(@PathParam("id") Long id);

    @GET
    @Path("/modules")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Modules getModules(
            @QueryParam("filter") String filter,
            @QueryParam("order") String order,
            @QueryParam("fieldName") String fieldName,
            @QueryParam("itemByPage") Integer itemByPage,
            @QueryParam("page") Integer page);

    @GET
    @Path("/modules/latest")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Modules getUpdatedModules(Modules modules);
}
