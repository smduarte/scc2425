package main.java.tukano.impl.rest;

import java.util.List;

import jakarta.inject.Singleton;
import main.java.tukano.api.Short;
import main.java.tukano.api.Shorts;
import main.java.tukano.api.rest.RestShorts;
import main.java.tukano.impl.JavaShorts;
import main.java.utils.StorageFactory;

@Singleton
public class RestShortsResource extends RestResource implements RestShorts {

	static final Shorts shortsBackend = StorageFactory.getShortsBackend();
	@Override
	public Short createShort(String userId, String password) {
		return super.resultOrThrow( shortsBackend.createShort(userId, password));
	}

	@Override
	public void deleteShort(String shortId, String password) {
		super.resultOrThrow( shortsBackend.deleteShort(shortId, password));
	}

	@Override
	public Short getShort(String shortId) {
		return super.resultOrThrow( shortsBackend.getShort(shortId));
	}
	@Override
	public List<String> getShorts(String userId) {
		return super.resultOrThrow( shortsBackend.getShorts(userId));
	}

	@Override
	public void follow(String userId1, String userId2, boolean isFollowing, String password) {
		super.resultOrThrow( shortsBackend.follow(userId1, userId2, isFollowing, password));
	}

	@Override
	public List<String> followers(String userId, String password) {
		return super.resultOrThrow( shortsBackend.followers(userId, password));
	}

	@Override
	public void like(String shortId, String userId, boolean isLiked, String password) {
		super.resultOrThrow( shortsBackend.like(shortId, userId, isLiked, password));
	}

	@Override
	public List<String> likes(String shortId, String password) {
		return super.resultOrThrow( shortsBackend.likes(shortId, password));
	}

	@Override
	public List<String> getFeed(String userId, String password) {
		return super.resultOrThrow( shortsBackend.getFeed(userId, password));
	}

	@Override
	public void deleteAllShorts(String userId, String password, String token) {
		super.resultOrThrow( shortsBackend.deleteAllShorts(userId, password, token));
	}	
}
