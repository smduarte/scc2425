package main.java.utils;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CosmosDBClient {
    private static final CosmosClient nosqlClient;
    private static final Connection postgresConnection;

    static {
        // Here we initialize NoSql CosmosDB client
        nosqlClient = new CosmosClientBuilder()
                .endpoint("") // example: cosmosdb-nosql-numero
                .key("") // example: numero aleatorio (primary key)
                .buildClient();

        // Here we initialize PostgreSQL connection for CosmosDB
        try {
            postgresConnection = DriverManager.getConnection(
                    "", // example: postgres://citus:{here the password}@c-cluster-postgre  || citus is the name of the database
                    "citus", // database name
                    "" // the password
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to PostgreSQL Cosmos DB", e);
        }
    }

    public static CosmosContainer getNoSQLContainer(String containerName) {
        return nosqlClient.getDatabase("cosmos.database").getContainer(containerName);
    }

    public static Connection getPostgresConnection() {
        return postgresConnection;
    }
}
