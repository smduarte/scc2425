package main.java.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCache {
    private static final String RedisHostname = "scc7066270663cache.redis.cache.windows.net"; // put the redis hostname
    private static final String RedisKey = "deDbQ5aU4YlQek6WlIiUTzvOtFZStkTVuAzCaPaD6UA="; // put the redis key
    private static final int REDIS_PORT = 6380;  // Default for Azure
    private static final int REDIS_TIMEOUT = 5000;
    private static final boolean Redis_USE_TLS = true;

    private static JedisPool instance;

    public synchronized static JedisPool getCachePool() {
        if (instance != null)
            return instance;

        var poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        try {
            instance = new JedisPool(poolConfig, RedisHostname, REDIS_PORT, REDIS_TIMEOUT, RedisKey, Redis_USE_TLS);
        } catch(Exception e) {
            System.err.println("Error creating Jedis pool: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unable to create Redis pool", e);  // Re-throw or handle the exception
        }
        return instance;
    } 
}