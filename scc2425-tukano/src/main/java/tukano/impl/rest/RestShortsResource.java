package tukano.impl.rest;

import jakarta.inject.Singleton;
import tukano.api.Short;
import tukano.api.Shorts;
import tukano.api.rest.RestShorts;
import tukano.impl.JavaShorts;
import utils.JSON;
import utils.RedisCache;

import java.util.List;

@Singleton
public class RestShortsResource extends RestResource implements RestShorts {

    static final Shorts impl = JavaShorts.getInstance();

    @Override
    public Short createShort(String userId, String password) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = "short:" + userId;
            var value = jedis.get(key);
            if (value != null) {
                return JSON.decode(value, Short.class);
            }
            var shorty = super.resultOrThrow(impl.createShort(userId, password));
            jedis.set(key, JSON.encode(shorty));
            return shorty;
        }
    }

    @Override
    public void deleteShort(String shortId, String password) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = "short:" + shortId;
            var value = jedis.get(key);
            if (value != null) {
                jedis.del(key);
            }
            super.resultOrThrow(impl.deleteShort(shortId, password));
        }
    }

    @Override
    public Short getShort(String shortId) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = "short:" + shortId;
            var value = jedis.get(key);
            if (value != null) {
                return JSON.decode(value, Short.class);
            }
            var shorty = super.resultOrThrow(impl.getShort(shortId));
            jedis.set(key, JSON.encode(shorty));
            return shorty;
        }
    }

    @Override
    public List<String> getShorts(String userId) {
        return super.resultOrThrow(impl.getShorts(userId));
    }

    @Override
    public void follow(String userId1, String userId2, boolean isFollowing, String password) {
        super.resultOrThrow(impl.follow(userId1, userId2, isFollowing, password));
    }

    @Override
    public List<String> followers(String userId, String password) {
        return super.resultOrThrow(impl.followers(userId, password));
    }

    @Override
    public void like(String shortId, String userId, boolean isLiked, String password) {
        super.resultOrThrow(impl.like(shortId, userId, isLiked, password));
    }

    @Override
    public List<String> likes(String shortId, String password) {
        return super.resultOrThrow(impl.likes(shortId, password));
    }

    @Override
    public List<String> getFeed(String userId, String password) {
        return super.resultOrThrow(impl.getFeed(userId, password));
    }

    @Override
    public void deleteAllShorts(String userId, String password, String token) {
        super.resultOrThrow(impl.deleteAllShorts(userId, password, token));
    }
}
