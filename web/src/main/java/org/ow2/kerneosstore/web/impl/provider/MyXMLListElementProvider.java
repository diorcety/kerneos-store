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

package org.ow2.kerneosstore.web.impl.provider;

import com.sun.jersey.core.provider.jaxb.AbstractListElementProvider;
import com.sun.jersey.spi.inject.Injectable;
import org.ow2.kerneosstore.web.CategoryElement;
import org.ow2.kerneosstore.web.ModuleElement;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;

@Provider
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
public class MyXMLListElementProvider extends AbstractListElementProvider {
    // Delay construction of factory
    private final Injectable<XMLInputFactory> xif;

    public MyXMLListElementProvider(@Context Injectable<XMLInputFactory> xif, @Context Providers ps) {
        super(ps, MediaType.APPLICATION_XML_TYPE);

        this.xif = xif;
    }

    @Override
    protected final XMLStreamReader getXMLStreamReader(Class<?> elementType,
                                                       MediaType mediaType,
                                                       Unmarshaller u,
                                                       InputStream entityStream)
            throws XMLStreamException {
        return xif.getValue().createXMLStreamReader(entityStream);
    }

    public final void writeList(Class<?> elementType, Collection<?> t,
                                MediaType mediaType, Charset c,
                                Marshaller m, OutputStream entityStream)
            throws JAXBException, IOException {
        String rootElement = null;
        final String cName = c.name();

        // Force root element name
        if (elementType.equals(CategoryElement.class)) {
            rootElement = "categories";
        } else if (elementType.equals(ModuleElement.class)) {
            rootElement = "modules";
        } else {
            rootElement = getRootElementName(elementType);
        }

        entityStream.write(
                String.format("<?xml version=\"1.0\" encoding=\"%s\" standalone=\"yes\"?>", cName).getBytes(cName));
        String property = "com.sun.xml.bind.xmlHeaders";
        String header;
        try {
            header = (String) m.getProperty(property);
        } catch (PropertyException e) {
            property = "com.sun.xml.internal.bind.xmlHeaders";
            header = (String) m.getProperty(property);
        }
        if (header != null) {
            m.setProperty(property, "");
            entityStream.write(header.getBytes(cName));
        }
        entityStream.write(String.format("<%s>", rootElement).getBytes(cName));
        for (Object o : t)
            m.marshal(o, entityStream);

        entityStream.write(String.format("</%s>", rootElement).getBytes(cName));
    }
}
