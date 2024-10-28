package main.java.tukano.impl;

import main.java.tukano.api.Result;
import main.java.tukano.api.User;
import main.java.tukano.api.Users;
import main.java.utils.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.logging.Logger;

public class PostgresUsers implements Users {

    private static final Logger log = Logger.getLogger(PostgresUsers.class.getName());
    private static PostgresUsers instance;

    synchronized public static Users getInstance() {
        if( instance == null )
            instance = new PostgresUsers();
        return instance;
    }

    @Override
    public Result<String> createUser(User user) {
        return Hibernate.getInstance().execute(session -> {
            session.persist(user);
            log.info("User created with ID: " + user.getUserId() );
            return Result.ok(user.getUserId());
        });
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        Result<User> userResult = Hibernate.getInstance().getOne(userId, User.class);

        if (!userResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        User user = userResult.value();
        if (user.getPwd().equals(pwd)) {
            return Result.ok(user);
        } else {
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }
    }


    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {
        if (user == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        Result<User> existingUserResult = Hibernate.getInstance().getOne(userId, User.class);

        if (!existingUserResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // User not found
        }

        User existingUser = existingUserResult.value(); // Get the current user

        if (!existingUser.getPwd().equals(pwd)) {
            return Result.error(Result.ErrorCode.FORBIDDEN); // Password mismatch
        }

        // Update the user details
        existingUser.setEmail(user.getEmail());
        existingUser.setDisplayName(user.getDisplayName());

        Result<User> updateResult = Hibernate.getInstance().updateOne(existingUser);
        return updateResult; // Return the result of the update operation
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        Result<User> existingUserResult = Hibernate.getInstance().getOne(userId, User.class);

        if (!existingUserResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // User not found
        }

        User existingUser = existingUserResult.value(); // Get existing user

        if (existingUser.getPwd().equals(pwd)) {
            return Hibernate.getInstance().deleteOne(existingUser); // Delete user
        } else {
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {

        // Prepare the SQL query
        String sql = "SELECT * FROM User WHERE LOWER(userId) LIKE LOWER('%" + pattern + "%')";

        // Execute the query
        List<User> users = Hibernate.getInstance().sql(sql, User.class);

        // Return OK with an empty list if no users were found
        if (users.isEmpty()) {
            return Result.ok(users);
        }

        // Iterate over the found users and set their passwords to an empty string
        for (User user : users) {
            user.setPwd(""); // is better for privacy
        }

        // Return the result
        return Result.ok(users);
    }
}
