package tukano.impl;

import tukano.api.Result;
import tukano.api.User;
import tukano.api.Users;
import utils.CosmosDB;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.lang.String.format;
import static tukano.api.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.Result.ErrorCode.FORBIDDEN;
import static tukano.api.Result.*;

public class JavaUsers implements Users {

    private static final Logger Log = Logger.getLogger(JavaUsers.class.getName());

    private static Users instance;

    private JavaUsers() {
    }

    synchronized public static Users getInstance() {
        if (instance == null)
            instance = new JavaUsers();
        return instance;
    }

    @Override
    public Result<String> createUser(User user) {
        Log.info(() -> format("createUser : %s\n", user));

        if (badUserInfo(user))
            return error(BAD_REQUEST);

        return errorOrValue(CosmosDB.insertOne(User.class, user), user.getId());
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        Log.info(() -> format("getUser : userId = %s, pwd = %s\n", userId, pwd));

        if (userId == null)
            return error(BAD_REQUEST);
        
        return validatedUserOrError(CosmosDB.getOne(User.class, USER_ID_KEY, userId, User.class), pwd);
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User other) {
        Log.info(() -> format("updateUser : userId = %s, pwd = %s, user: %s\n", userId, pwd, other));

        if (badUpdateUserInfo(userId, pwd, other))
            return error(BAD_REQUEST);

        return errorOrResult(
                validatedUserOrError(CosmosDB.getOne(User.class, USER_ID_KEY, userId, User.class), pwd), user ->
                        CosmosDB.updateOne(User.class, user.updateFrom(other))
        );
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        Log.info(() -> format("deleteUser : userId = %s, pwd = %s\n", userId, pwd));

        if (userId == null || pwd == null)
            return error(BAD_REQUEST);

        return errorOrResult(validatedUserOrError(CosmosDB.getOne(User.class, USER_ID_KEY, userId, User.class), pwd), user -> {

            // Delete user shorts and related info asynchronously in a separate thread
            Executors.defaultThreadFactory().newThread(() -> {
                JavaShorts.getInstance().deleteAllShorts(userId, pwd, Token.get(userId));
                JavaBlobs.getInstance().deleteAllBlobs(userId, Token.get(userId));
            }).start();

            return CosmosDB.deleteOne(User.class, user);
        });
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Log.info(() -> format("searchUsers : patterns = %s\n", pattern));

        var query = format("SELECT * FROM User u WHERE UPPER(u.userId) LIKE '%%%s%%'", pattern.toUpperCase());
        var hits = CosmosDB.query(User.class, query, User.class)
                .stream()
                .map(User::copyWithoutPassword)
                .toList();

        return ok(hits);
    }


    private Result<User> validatedUserOrError(Result<User> res, String pwd) {
        if (res.isOK())
            return res.value().getPwd().equals(pwd) ? res : error(FORBIDDEN);
        else
            return res;
    }

    private boolean badUserInfo(User user) {
        return (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null);
    }

    private boolean badUpdateUserInfo(String userId, String pwd, User info) {
        return (userId == null || pwd == null || info.getId() != null && !userId.equals(info.getId()));
    }
}
