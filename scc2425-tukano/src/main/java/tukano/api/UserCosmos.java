package main.java.tukano.api;

import jakarta.persistence.Entity;
@Entity
public class UserCosmos extends User {
    private String id; // This will be the id field for Cosmos DB

    public UserCosmos() {
        super();
    }

    public UserCosmos(User user) {
        super(user.userId(), user.pwd(), user.email(), user.displayName());
        this.id = user.userId(); // Set the id as userId
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UserWithId [userId=" + getUserId() + ", pwd=" + getPwd() +
                ", email=" + getEmail() + ", displayName=" + getDisplayName() + "]";
    }
}
