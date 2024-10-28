package main.java.tukano.impl.rest;

import java.util.List;

import jakarta.inject.Singleton;
import main.java.tukano.api.User;
import main.java.tukano.api.Users;
import main.java.tukano.api.rest.RestUsers;
import main.java.tukano.impl.JavaUsers;
import main.java.utils.StorageFactory;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers {

	final Users usersBackend;
	public RestUsersResource() {
		this.usersBackend = StorageFactory.getUsersBackend();
	}
	
	@Override
	public String createUser(User user) {
		return super.resultOrThrow(usersBackend.createUser(user));
	}

	@Override
	public User getUser(String name, String pwd) {
		return super.resultOrThrow( usersBackend.getUser(name, pwd));
	}
	
	@Override
	public User updateUser(String name, String pwd, User user) {
		return super.resultOrThrow( usersBackend.updateUser(name, pwd, user));
	}

	@Override
	public User deleteUser(String name, String pwd) {
		return super.resultOrThrow( usersBackend.deleteUser(name, pwd));
	}

	@Override
	public List<User> searchUsers(String pattern) {
		return super.resultOrThrow( usersBackend.searchUsers( pattern));
	}
}
