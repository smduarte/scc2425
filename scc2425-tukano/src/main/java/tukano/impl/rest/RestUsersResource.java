package tukano.impl.rest;

import jakarta.inject.Singleton;
import tukano.api.User;
import tukano.api.Users;
import tukano.api.rest.RestUsers;
import tukano.impl.JavaUsers;
import utils.JSON;
import utils.RedisCache;

import java.util.List;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers {

    final Users impl;

    public RestUsersResource() {
        this.impl = JavaUsers.getInstance();
    }

    @Override
    public String createUser(User user) {
        return super.resultOrThrow(impl.createUser(user));
    }

    @Override
    public User getUser(String name, String pwd) {
        try(var jedis = RedisCache.getCachePool().getResource()) {
            var key = "user:" + name;
            var value = jedis.get(key);
            if(value != null) {
                return JSON.decode(value, User.class);
            }
            var user = super.resultOrThrow(impl.getUser(name, pwd));
            jedis.set(key, JSON.encode(user));
            return user;
        }
    }

    @Override
    public User updateUser(String name, String pwd, User user) {
        try(var jedis = RedisCache.getCachePool().getResource()) {
            var key = "user:" + name;
            var value = jedis.get(key);
            if(value != null) {
                jedis.del(key);
            }
            var updatedUser = super.resultOrThrow(impl.updateUser(name, pwd, user));
            jedis.set(key, JSON.encode(user));
            return updatedUser;
        }
    }

    @Override
    public User deleteUser(String name, String pwd) {
        try(var jedis = RedisCache.getCachePool().getResource()) {
            var key = "user:" + name;
            var value = jedis.get(key);
            if(value != null) {
                jedis.del(key);
            }
            return super.resultOrThrow(impl.deleteUser(name, pwd));
        }
    }

    @Override
    public List<User> searchUsers(String pattern) {
        return super.resultOrThrow(impl.searchUsers(pattern));
    }
}
