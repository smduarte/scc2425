package main.java.utils;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import main.java.tukano.api.Result;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import main.java.tukano.api.Result.ErrorCode;

public class CosmosNoSQL {

    private static final String CONNECTION_URL = "https://cosmos7066270663.documents.azure.com:443/"; // replace with your own
    private static final String DB_KEY = "ZU8GyphhOkRCUcqFlEUrDdhqDuILhyX9tRzbWmr0hbHkZwahcvUZF5P7BovcGPrsEAuiKYarckNTACDb80vegQ==";
    private static final String DB_NAME = "scc7066270663";
    private static final String CONTAINER = "users_shorts";

    private static CosmosNoSQL instance;

    public static synchronized CosmosNoSQL getInstance() {
        if( instance != null)
            return instance;

        CosmosClient client = new CosmosClientBuilder()
                .endpoint(CONNECTION_URL)
                .key(DB_KEY)
                //.directMode()
                .gatewayMode()
                // replace by .directMode() for better performance
                .consistencyLevel(ConsistencyLevel.SESSION)
                .connectionSharingAcrossClientsEnabled(true)
                .contentResponseOnWriteEnabled(true)
                .buildClient();
        instance = new CosmosNoSQL( client);
        return instance;

    }

    private CosmosClient client;
    private CosmosDatabase db;
    private CosmosContainer container;

    public CosmosNoSQL(CosmosClient client) {
        this.client = client;
    }

    private synchronized void init() {
        if( db != null)
            return;
        db = client.getDatabase(DB_NAME);
        container = db.getContainer(CONTAINER);
    }

    public void close() {
        client.close();
    }

    public <T> Result<List<T>> sql(String queryStr, Class<T> clazz) {
        return query(clazz, queryStr);
    }

    public <T> Result<List<T>> sql(Class<T> clazz, String fmt, Object... args) {
        String formattedQuery = String.format(fmt, args);
        return sql(formattedQuery, clazz);
    }

    public <T> Result<T> getOne(String id, Class<T> clazz) {
        return tryCatch( () -> container.readItem(id, new PartitionKey(id), clazz).getItem());
    }

    public <T> Result<T> deleteOne(T obj) {
            var result = container.deleteItem(obj, new CosmosItemRequestOptions());
            if(result.getStatusCode() == 204) {
                return Result.ok(obj);
            }
            else {
                return Result.error(errorCodeFromStatus(result.getStatusCode()));
            }
    }

    public <T> Result<T> updateOne(T obj) {
        return tryCatch( () -> container.upsertItem(obj).getItem());
    }

    public <T> Result<T> insertOne( T obj) {
        System.err.println("NoSQL.insert:" + obj );
        return tryCatch( () -> container.createItem(obj).getItem());
    }

    public <T> Result<T> runOperations(List<Runnable> operations) {
        for (Runnable operation : operations) {
            operation.run();
        }
        return Result.ok();
    }

    private <T> Result<List<T>> query(Class<T> clazz, String queryStr) {
        return tryCatch(() -> {
            var res = container.queryItems(queryStr, new CosmosQueryRequestOptions(), clazz);
            return res.stream().toList();
        });
    }

    <T> Result<T> tryCatch( Supplier<T> supplierFunc) {
        try {
            init();
            return Result.ok(supplierFunc.get());
        } catch( CosmosException ce ) {
            System.err.println("Cosmos Exception caused by: ");
            ce.printStackTrace();
            return Result.error ( errorCodeFromStatus(ce.getStatusCode() ));
        } catch( Exception x ) {
            System.err.println("Exception caused by: ");
            x.printStackTrace();
            return Result.error( Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    static Result.ErrorCode errorCodeFromStatus( int status ) {
        return switch( status ) {
            case 200 -> Result.ErrorCode.OK;
            case 404 -> Result.ErrorCode.NOT_FOUND;
            case 409 -> Result.ErrorCode.CONFLICT;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }

}
