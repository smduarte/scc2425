package main.java.utils;

import main.java.tukano.api.Shorts;
import main.java.tukano.api.Users;
import main.java.tukano.impl.JavaShorts;
import main.java.tukano.impl.JavaUsers;
import main.java.tukano.impl.HibernateShorts;
import main.java.tukano.impl.HibernateUsers;

public class StorageFactory {

    // Define the type of the backend, can be either "nosql" or "postgres"
    private static final String BACKEND_TYPE = "nosql"; // if we want to use postgres change it for postgres
    public static Users getUsersBackend() {
        // This is a better approach but to not store environmental variables we use the static String above
        // String backendType = System.getenv("storage.backend"); // Set to "nosql" or "postgres"

        // This code because when the type is in environmental variables, it will fetch the corresponding string
        if ("nosql".equalsIgnoreCase(BACKEND_TYPE)) {
            return JavaUsers.getInstance();
        } else if ("postgres".equalsIgnoreCase(BACKEND_TYPE)) {
            return HibernateUsers.getInstance();
        } else {
            throw new IllegalArgumentException("Unsupported backend type: " + BACKEND_TYPE);
        }
    }

    public static Shorts getShortsBackend() {
        // This is a better approach but to not store environmental variables we use the static String
        // String backendType = System.getenv("storage.backend"); // Set to "nosql" or "postgres"

        // This code because when the type is in environmental variables, it will fetch the corresponding string
        if ("nosql".equalsIgnoreCase(BACKEND_TYPE)) {
            return JavaShorts.getInstance();
        } else if ("postgres".equalsIgnoreCase(BACKEND_TYPE)) {
            return HibernateShorts.getInstance();
        } else {
            throw new IllegalArgumentException("Unsupported backend type: " + BACKEND_TYPE);
        }
    }
}
