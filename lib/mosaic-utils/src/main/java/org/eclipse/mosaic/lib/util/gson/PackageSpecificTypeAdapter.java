package org.eclipse.mosaic.lib.util.gson;

import org.eclipse.mosaic.lib.gson.AbstractTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;

import java.util.LinkedList;
import java.util.List;

public class PackageSpecificTypeAdapter<T> extends AbstractTypeAdapterFactory<T> {
    private final List<String> packageNames = new LinkedList<>();

    public PackageSpecificTypeAdapter(TypeAdapterFactory parentFactory, Gson gson) {
        super(parentFactory, gson);
    }

    public PackageSpecificTypeAdapter<T> searchInPackageOfClass(Class<?> clazz) {
        return searchInPackage(clazz.getPackage());
    }

    public PackageSpecificTypeAdapter<T> searchInPackage(Package searchPackage) {
        return searchInPackage(searchPackage.getName());
    }

    public PackageSpecificTypeAdapter<T> searchInPackage(String packageName) {
        packageNames.add(packageName);
        return this;
    }

    @Override
    protected Class<?> fromTypeName(String type) {
        Class<?> returnClass = null;
        for (String searchPackage : packageNames) {
            try {
                returnClass = Class.forName(searchPackage + "." + type);
            } catch (ClassNotFoundException ignored) {
                // nop
            }
            if (returnClass != null) {
                break;
            }
        }
        if (returnClass != null) {
            return returnClass;
        } else {
            throw new JsonParseException("Cannot deserialize TraversalBasedProcessor " + type);
        }
    }

    @Override
    protected String toTypeName(Class<?> typeClass) {
        return typeClass.getSimpleName();
    }
}