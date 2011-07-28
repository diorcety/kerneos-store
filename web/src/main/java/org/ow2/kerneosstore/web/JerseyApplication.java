package org.ow2.kerneosstore.web;

import org.ow2.kerneosstore.web.impl.StoreImpl;
import org.ow2.kerneosstore.web.impl.StoreInfo;

import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

public class JerseyApplication extends Application {

    private StoreInfo storeInfo;

    public JerseyApplication(StoreInfo storeInfo) {
        this.storeInfo = storeInfo;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = new HashSet<Object>();
        objects.add(new StoreImpl(storeInfo));
        return objects;
    }
}