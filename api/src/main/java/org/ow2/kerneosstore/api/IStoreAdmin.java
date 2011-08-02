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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface IStoreAdmin {

    ///////////////////////////////////////////////////////////////////////////
    /// Store
    ///////////////////////////////////////////////////////////////////////////

    @POST
    @Path("/info")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public StoreInfo setInfo(StoreInfo storeInfo);


    ///////////////////////////////////////////////////////////////////////////
    /// Module
    ///////////////////////////////////////////////////////////////////////////

    @POST
    @Path("/module")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Module setModule(Module module);

    @POST
    @Path("/module/{id}/{major}/{minor}/{revision}/image")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] setModuleImage(@PathParam("id") Long id,
                                 @PathParam("major") Integer major,
                                 @PathParam("minor") Integer minor,
                                 @PathParam("revision") Integer revision, byte[] data);

    @GET
    @Path("/module/{id}/{major}/{minor}/{revision}/image")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] getModuleImage(@PathParam("id") Long id,
                                 @PathParam("major") Integer major,
                                 @PathParam("minor") Integer minor,
                                 @PathParam("revision") Integer revision);

    @DELETE
    @Path("/module/{id}/{major}/{minor}/{revision}/image")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] removeModuleImage(@PathParam("id") Long id,
                                    @PathParam("major") Integer major,
                                    @PathParam("minor") Integer minor,
                                    @PathParam("revision") Integer revision);

    @DELETE
    @Path("/module/{id}/{major}/{minor}/{revision}")
    @Produces(MediaType.APPLICATION_XML)
    public Module removeModule(@PathParam("id") Long id,
                               @PathParam("major") Integer major,
                               @PathParam("minor") Integer minor,
                               @PathParam("revision") Integer revision);

    @GET
    @Path("/module/{id}/{major}/{minor}/{revision}")
    @Produces(MediaType.APPLICATION_XML)
    public Module getModule(@PathParam("id") Long id,
                            @PathParam("major") Integer major,
                            @PathParam("minor") Integer minor,
                            @PathParam("revision") Integer revision);

    @GET
    @Path("/module/{id}/{major}/{minor}/{revision}/enable")
    @Produces(MediaType.APPLICATION_XML)
    public Module enableModule(@PathParam("id") Long id,
                               @PathParam("major") Integer major,
                               @PathParam("minor") Integer minor,
                               @PathParam("revision") Integer revision);


    @GET
    @Path("/module/{id}/{major}/{minor}/{revision}/disable")
    @Produces(MediaType.APPLICATION_XML)
    public Module disableModule(@PathParam("id") Long id,
                                @PathParam("major") Integer major,
                                @PathParam("minor") Integer minor,
                                @PathParam("revision") Integer revision);

    @GET
    @Path("/modules/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Modules getModules(@PathParam("id") Long id);


    ///////////////////////////////////////////////////////////////////////////
    /// Repository
    ///////////////////////////////////////////////////////////////////////////

    @POST
    @Path("/repository")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Repository setRepository(Repository repository);

    @GET
    @Path("/repository/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Repository getRepository(@PathParam("id") Long id);

    @POST
    @Path("/repository/{repositoryId}/module/{id}/{major}/{minor}/{revision}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String setRepositoryEntity(@PathParam("repositoryId") Long repositoryId,
                                      @PathParam("id") Long id,
                                      @PathParam("major") Integer major,
                                      @PathParam("minor") Integer minor,
                                      @PathParam("revision") Integer revision,
                                      String key);

    @DELETE
    @Path("/repository/{repositoryId}/module/{id}/{major}/{minor}/{revision}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String removeRepositoryEntity(@PathParam("repositoryId") Long repositoryId,
                                         @PathParam("id") Long id,
                                         @PathParam("major") Integer major,
                                         @PathParam("minor") Integer minor,
                                         @PathParam("revision") Integer revision);

    @DELETE
    @Path("/repository/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Repository removeRepository(@PathParam("id") Long id);
}
