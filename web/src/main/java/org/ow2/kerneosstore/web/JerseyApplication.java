package org.ow2.kerneosstore.web;

import org.ow2.kerneosstore.core.Store;
import org.ow2.kerneosstore.web.impl.StoreImpl;

import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

public class JerseyApplication extends Application {

    private Store store;

    public JerseyApplication(Store store) {
        this.store = store;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = new HashSet<Object>();
        objects.add(new StoreImpl(store));
        return objects;
    }
}