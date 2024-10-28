package main.java.tukano.impl;

import main.java.tukano.api.*;
import main.java.tukano.api.Short;
import main.java.utils.Hibernate;

import java.util.List;
import java.util.logging.Logger;

public class PostgresShorts implements Shorts {

    private static final Logger log = Logger.getLogger(PostgresShorts.class.getName());
    private static PostgresShorts instance;

    synchronized public static Shorts getInstance() {
        if( instance == null )
            instance = new PostgresShorts();
        return instance;
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        // First we handle the user part
        Result<User> userResult = Hibernate.getInstance().getOne(userId, User.class);
        if (!userResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // User not found
        }

        User user = userResult.value();
        if (!user.getPwd().equals(password)) {
            return Result.error(Result.ErrorCode.FORBIDDEN); // Incorrect password
        }

        // Create a new Short
        String shortId = generateUniqueShortId(); // Generates a unique short ID
        Short newShort = new Short(shortId, userId, "???"); // MODIFY HERE

        // Persist the Short to the database
        Result<Short> shortResult = Hibernate.getInstance().execute(session -> {
            session.persist(newShort);
            log.info("Short created with ID: " + shortId);
            return Result.ok(newShort);
        });

        return shortResult;
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        // Get the short to be deleted
        Result<Short> shortResult = Hibernate.getInstance().getOne(shortId, Short.class);
        if (!shortResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // Short not found
        }

        Short existingShort = shortResult.value();

        // Verify that the user owns the Short
        Result<User> userResult = Hibernate.getInstance().getOne(existingShort.getOwnerId(), User.class);
        if (!userResult.isOK() || !userResult.value().getPwd().equals(password)) {
            return Result.error(Result.ErrorCode.FORBIDDEN); // Incorrect password or user not found
        }

        // Delete the Short from the database
        Hibernate.getInstance().deleteOne(existingShort);

        // Return OK if deletion was successful
        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        // Get the short
        Result<Short> shortResult = Hibernate.getInstance().getOne(shortId, Short.class);

        // Check if the Short was found
        if (!shortResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // Short not found
        }

        // Return the retrieved Short if found
        return Result.ok(shortResult.value());
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        // Check if the user exists first
        Result<User> userResult = Hibernate.getInstance().getOne(userId, User.class);
        if (!userResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // User not found
        }

        // SQL query to retrieve short IDs for the specified user
        // Not sure if this is right
        String sql = "SELECT shortId FROM Short WHERE ownerId = :userId";

        // Assuming the sql() method is modified to allow a single named parameter
        List<String> shorts = Hibernate.getInstance().sql(sql, String.class);

        return Result.ok(shorts);
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        // Check if both users exist
        Result<User> userResult1 = Hibernate.getInstance().getOne(userId1, User.class);
        Result<User> userResult2 = Hibernate.getInstance().getOne(userId2, User.class);

        if (!userResult1.isOK() || !userResult2.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // One or both users not found
        }

        // Verify if user1 password is correct
        if (!userResult1.value().getPwd().equals(password)) {
            return Result.error(Result.ErrorCode.FORBIDDEN); // Incorrect password
        }

        // Logic to follow or unfollow the other user
        if (isFollowing) {
            // Add to follow list
            String sql = "INSERT INTO Follows (followerId, followedId) VALUES ('" + userId1 + "', '" + userId2 + "')";
            Hibernate.getInstance().sql(sql, Void.class);
        } else {
            // Remove from follow list
            String sql = "DELETE FROM Follows WHERE followerId = '" + userId1 + "' AND followedId = '" + userId2 + "'";
            Hibernate.getInstance().sql(sql, Void.class);
        }

        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        // Check if the user exists
        Result<User> userResult = Hibernate.getInstance().getOne(userId, User.class);
        if (!userResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // User not found
        }

        // Verify password for the user
        if (!userResult.value().getPwd().equals(password)) {
            return Result.error(Result.ErrorCode.FORBIDDEN); // Incorrect password
        }

        // SQL to get the list of followers
        String sql = "SELECT followerId FROM Follows WHERE followedId = '" + userId + "'";
        List<String> followers = Hibernate.getInstance().sql(sql, String.class);

        return Result.ok(followers);
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        // Check if the short exists
        Result<Short> shortResult = Hibernate.getInstance().getOne(shortId, Short.class);
        if (!shortResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // Short not found
        }

        // Check if the user exists and verify password
        Result<User> userResult = Hibernate.getInstance().getOne(userId, User.class);
        if (!userResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // User not found
        }
        if (!userResult.value().getPwd().equals(password)) {
            return Result.error(Result.ErrorCode.FORBIDDEN); // Incorrect password
        }

        // Logic to like or unlike the short
        if (isLiked) {
            // Add like
            String sql = "INSERT INTO Likes (shortId, userId) VALUES ('" + shortId + "', '" + userId + "')";
            Hibernate.getInstance().sql(sql, Void.class);
        } else {
            // Remove like
            String sql = "DELETE FROM Likes WHERE shortId = '" + shortId + "' AND userId = '" + userId + "'";
            Hibernate.getInstance().sql(sql, Void.class);
        }

        return Result.ok();
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        // Check if the short exists
        Result<Short> shortResult = Hibernate.getInstance().getOne(shortId, Short.class);
        if (!shortResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // Short not found
        }

        // SQL to get the list of users who liked the short
        String sql = "SELECT userId FROM Likes WHERE shortId = '" + shortId + "'";
        List<String> likes = Hibernate.getInstance().sql(sql, String.class); // Retrieve list of userIds who liked the short

        return Result.ok(likes);
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        // Check if the user exists
        Result<User> userResult = Hibernate.getInstance().getOne(userId, User.class);
        if (!userResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // User not found
        }

        // Verify password for the user
        if (!userResult.value().getPwd().equals(password)) {
            return Result.error(Result.ErrorCode.FORBIDDEN); // Incorrect password
        }

        // SQL to get the shorts from followed users
        String shortsSql = "SELECT s.shortId " +
                "FROM Shorts s " +
                "JOIN Follows f ON s.ownerId = f.followedId " +
                "WHERE f.followerId = '" + userId + "' " +
                "ORDER BY s.timestamp DESC";

        // Retrieve the feed
        List<String> feed = Hibernate.getInstance().sql(shortsSql, String.class);

        return Result.ok(feed); // Return the feed
    }

    @Override
    public Result<Void> deleteAllShorts(String userId, String password, String token) {
        // Check if the user exists
        Result<User> userResult = Hibernate.getInstance().getOne(userId, User.class);
        if (!userResult.isOK()) {
            return Result.error(Result.ErrorCode.NOT_FOUND); // User not found
        }

        // Verify password for the user
        if (!userResult.value().getPwd().equals(password)) {
            return Result.error(Result.ErrorCode.FORBIDDEN); // Incorrect password
        }

        // SQL to delete all shorts by the user
        String deleteSql = "DELETE FROM Shorts WHERE ownerId = '" + userId + "'";
        Hibernate.getInstance().sql(deleteSql, Void.class); // Execute delete operation

        return Result.ok(); // Return success
    }

    // Auxiliary method to generate unique Short ID (We can change this)
    // It's an example I found
    private String generateUniqueShortId() {
        return String.valueOf(System.currentTimeMillis());
    }
}
