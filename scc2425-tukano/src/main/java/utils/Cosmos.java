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
import com.azure.cosmos.models.CosmosBatchOperationResult;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.Consumer;
import main.java.tukano.api.Result.ErrorCode;

public class Cosmos {

    private static final String CONNECTION_URL = "https://scc242570663.documents.azure.com:443/"; // replace with your own
    private static final String DB_KEY = "a7f5jR6cQUdwKkbLkiiwr1W0AGwa7wtugj2NFMWIx8uxOzRLh4Lg4mn1oMNyArIswm700HvuFOqIACDbEWMVng==";
    private static final String DB_NAME = "scc2425";
    private static final String CONTAINER = "users";

    private static Cosmos instance;

    public static synchronized Cosmos getInstance() {
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
        instance = new Cosmos( client);
        return instance;

    }

    private CosmosClient client;
    private CosmosDatabase db;
    private CosmosContainer container;

    public Cosmos(CosmosClient client) {
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
        return tryCatch( () -> {
            container.deleteItem(obj, new CosmosItemRequestOptions());
            return obj;
        });
    }

    public <T> Result<T> updateOne(T obj) {
        return tryCatch( () -> container.upsertItem(obj).getItem());
    }

    public <T> Result<T> insertOne( T obj) {
        return tryCatch( () -> container.createItem(obj).getItem());
    }

    private <T> Result<List<T>> query(Class<T> clazz, String queryStr) {
        return tryCatch(() -> {
            var res = container.queryItems(queryStr, new CosmosQueryRequestOptions(), clazz);
            return res.stream().toList();
        });
    }

    public Result<Void> transaction(List<Runnable> operations, String partitionKeyValue) {
            PartitionKey partitionKey = new PartitionKey(partitionKeyValue);
            CosmosBatch batch = CosmosBatch.createCosmosBatch(partitionKey);
            for (Runnable operation : operations) {
                operation.run();  // Add operations to batch
            }
            CosmosBatchResponse response = container.executeCosmosBatch(batch);

            if (!response.isSuccessStatusCode()) {
                return Result.error(ErrorCode.CONFLICT);
            }

            return Result.ok();
    }

    <T> Result<T> tryCatch( Supplier<T> supplierFunc) {
        try {
            init();
            return Result.ok(supplierFunc.get());
        } catch( CosmosException ce ) {
            //ce.printStackTrace();
            return Result.error ( errorCodeFromStatus(ce.getStatusCode() ));
        } catch( Exception x ) {
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
