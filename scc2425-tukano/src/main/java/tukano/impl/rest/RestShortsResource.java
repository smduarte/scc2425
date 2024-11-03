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
    private static final String USER_SHORTS_KEY = "userShorts:";
    private static final String SHORT_KEY = "short:";
    private static final String USER_FOLLOWERS_KEY = "followers:";
    private static final String SHORT_LIKES_KEY = "likes:";

    @Override
    public Short createShort(String userId, String password) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            // creates a short unconditionally
            var shorty = super.resultOrThrow(impl.createShort(userId, password));

            // shorts id belonging to the user
            var key = USER_SHORTS_KEY + userId;
            if (jedis.exists(key)) {
                jedis.lpush(key, JSON.encode(shorty.getShortId()));
            } else {
                jedis.set(key, JSON.encode(shorty.getShortId()));
            }

            //shorts on cache
            var shortKey = SHORT_KEY + shorty.getShortId();
            jedis.set(shortKey, JSON.encode(shorty));
            return shorty;
        }
    }

    @Override
    public void deleteShort(String shortId, String password) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = SHORT_KEY + shortId;
            var value = jedis.get(key);
            Short shorty = null;
            if (value != null) {
                shorty = JSON.decode(value, Short.class);
                jedis.del(key);
            }
            var userShortsKey = USER_SHORTS_KEY + shorty.getOwnerId();
            jedis.lrem(userShortsKey, 0, shortId);
            super.resultOrThrow(impl.deleteShort(shortId, password));
        }
    }

    @Override
    public Short getShort(String shortId) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = SHORT_KEY + shortId;
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
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = USER_SHORTS_KEY + userId;
            var value = jedis.get(key);
            if (value != null) {
                return JSON.decode(value, List.class);
            }
            var shorts = super.resultOrThrow(impl.getShorts(userId));
            jedis.set(key, JSON.encode(shorts));
            return shorts;
        }
    }

    @Override
    public void follow(String userId1, String userId2, boolean isFollowing, String password) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            super.resultOrThrow(impl.follow(userId1, userId2, isFollowing, password));
            var key = USER_FOLLOWERS_KEY + userId2;
            if (isFollowing) {
                jedis.lpush(key, userId1);
            } else {
                jedis.lrem(key, 0, userId1);
            }
        }
    }

    @Override
    public List<String> followers(String userId, String password) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = USER_FOLLOWERS_KEY + userId;
            var value = jedis.get(key);
            if (value != null) {
                return JSON.decode(value, List.class);
            }
            var followers = super.resultOrThrow(impl.followers(userId, password));
            jedis.set(key, JSON.encode(followers));
            return followers;
        }
    }

    @Override
    public void like(String shortId, String userId, boolean isLiked, String password) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            super.resultOrThrow(impl.like(shortId, userId, isLiked, password));
            var key = SHORT_LIKES_KEY + shortId;
            if (isLiked) {
                jedis.lpush(key, userId);
            } else {
                jedis.lrem(key, 0, userId);
            }
        }
    }

    @Override
    public List<String> likes(String shortId, String password) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = SHORT_LIKES_KEY + shortId;
            var value = jedis.get(key);
            if (value != null) {
                return JSON.decode(value, List.class);
            }
            var likes = super.resultOrThrow(impl.likes(shortId, password));
            jedis.set(key, JSON.encode(likes));
            return likes;
        }
    }

    @Override
    public List<String> getFeed(String userId, String password) {
        return super.resultOrThrow(impl.getFeed(userId, password));
    }

    @Override
    public void deleteAllShorts(String userId, String password, String token) {
        try (var jedis = RedisCache.getCachePool().getResource()) {
            var key = USER_SHORTS_KEY + userId;
            var userShorts = jedis.get(key);
            if (userShorts != null) {
                var shorts = JSON.decode(userShorts, List.class);
                assert shorts != null;
                for (var shortId : shorts) {
                    var shortKey = SHORT_KEY + shortId;
                    var likesKey = SHORT_LIKES_KEY + shortId;
                    jedis.del(shortKey);
                    jedis.del(likesKey);
                }
                jedis.del(USER_FOLLOWERS_KEY + userId);
                jedis.del(key);

            }
            super.resultOrThrow(impl.deleteAllShorts(userId, password, token));
        }
    }
}
