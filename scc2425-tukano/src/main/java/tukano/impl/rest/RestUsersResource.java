package tukano.impl.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.inject.Singleton;
import tukano.api.User;
import tukano.api.Users;
import tukano.api.rest.RestUsers;
import tukano.impl.JavaUsers;
import utils.JSON;
import utils.RedisCache;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers {

    final Users impl;

    public RestUsersResource() {
        this.impl = JavaUsers.getInstance();
    }

    @Override
    public String createUser(User user) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = "user:" + user.displayName();
            var value = jedis.get(key);
            if (value != null) {
                return "CONFLICT";
            }
            var createdUser = super.resultOrThrow(impl.createUser(user));
            jedis.set(key, JSON.encode(user));
            return createdUser;
        }
    }

    @Override
    public User getUser(String name, String pwd) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = "user:" + name;
            var value = jedis.get(key);
            if (value != null) {
                return JSON.decode(value, User.class);
            }
            var user = super.resultOrThrow(impl.getUser(name, pwd));
            jedis.set(key, JSON.encode(user));
            return user;
        }
    }

    @Override
    public User updateUser(String name, String pwd, User user) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = "user:" + name;
            var value = jedis.get(key);
            if (value != null) {
                jedis.del(key);
            }
            var updatedUser = super.resultOrThrow(impl.updateUser(name, pwd, user));
            jedis.set(key, JSON.encode(user));
            return updatedUser;
        }
    }

    @Override
    public User deleteUser(String name, String pwd) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = "user:" + name;
            var value = jedis.get(key);
            if (value != null) {
                jedis.del(key);
            }
            return super.resultOrThrow(impl.deleteUser(name, pwd));
        }
    }

    /**
     * TODO needs revision, its going to both cache and db for data,
     *  strategy will be either complex or have data duplication on cache and even then it can be inconsistent
     * @param pattern
     * @return
     */
    @Override
    public List<User> searchUsers(String pattern) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = "search:" + pattern;
            var value = jedis.get(key);

            List<User> users = null;
            if (value != null) {
                users = JSON.decode(value, new TypeReference<List<User>>() {});
            }
            var dbUsers = super.resultOrThrow(impl.searchUsers(pattern));
            // merge both lists
            var allUsers = new ArrayList<>(users != null ? users : List.of());
            allUsers.addAll(dbUsers);
            if (value != null) {
                jedis.del(key);
            }
            jedis.set(key, JSON.encode(users));
            return allUsers;

        }
    }
}
