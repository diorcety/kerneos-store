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

package org.ow2.kerneosstore.web.util;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.Map;

public class AnyElementMapAdapter extends XmlAdapter<Object, Map<String, String>> {
    private static final DocumentBuilder df;

    static {
        try {
            df = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public AnyElementMapAdapter() {

    }

    @Override
    public Object marshal(Map<String, String> arg0) throws Exception {
        Document doc = df.newDocument();
        Element element = doc.createElement("map");

        for (Map.Entry<String, String> entry : arg0.entrySet()) {
            Element subElement = doc.createElement(entry.getKey());
            subElement.appendChild(doc.createTextNode(entry.getValue()));
            element.appendChild(subElement);
        }

        return element;
    }

    @Override
    public Map<String, String> unmarshal(Object arg0) throws Exception {
        if (arg0 instanceof Element) {
            Element element = (Element) arg0;
            HashMap<String, String> hashMap = new HashMap<String, String>();
            for (int i = 0; i < element.getChildNodes().getLength(); i++) {
                Node node = element.getChildNodes().item(i);
                if (node instanceof Element) {
                    Element subElement = (Element) node;
                    if (subElement.getFirstChild() != null)
                        hashMap.put(subElement.getNodeName(), subElement.getFirstChild().getNodeValue());
                }
            }
            return hashMap;
        }
        return null;
    }
}