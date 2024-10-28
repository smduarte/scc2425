package main.java.utils;

import java.util.List;


import main.java.tukano.api.Result;

public class CosmosDB {

    public static <T> List<T> sql(String query, Class<T> clazz) {
        return CosmosNoSQL.getInstance().sql(query, clazz).value();
    }


    public static <T> List<T> sql(Class<T> clazz, String fmt, Object ... args) {
        return CosmosNoSQL.getInstance().sql(String.format(fmt, args), clazz).value();
    }

    public static <T> Result<T> getOne(String id, Class<T> clazz) {
        return CosmosNoSQL.getInstance().getOne(id, clazz);
    }

    public static <T> Result<T> deleteOne(T obj) {
        return CosmosNoSQL.getInstance().deleteOne(obj);
    }

    public static <T> Result<T> updateOne(T obj) {
        return CosmosNoSQL.getInstance().updateOne(obj);
    }

    public static <T> Result<T> insertOne( T obj) {
        System.err.println("DB.insert:" + obj );
        return Result.errorOrValue(CosmosNoSQL.getInstance().insertOne(obj), obj);
    }

    public static Result<Void> transaction(List<Runnable> operations, String partitionKeyValue) {
        return CosmosNoSQL.getInstance().transaction(operations, partitionKeyValue);
    }
}
