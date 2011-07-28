package org.ow2.kerneosstore.api;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public interface IStoreInfo {

    public String getName();

    public String getDescription();

    public String getUrl();
}
