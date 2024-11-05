package main.java.tukano.impl.data;

import jakarta.persistence.Entity;

@Entity
public class LikesCosmos extends Likes{

    private String id;

    public LikesCosmos() {
        super();
    }
    public LikesCosmos( Likes l) {
        super(l.userId, l.shortId, l.ownerId);
        this.id = l.userId + l.shortId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "LikesCosmos [userId=" + getUserId() + ", shortId=" + getShortId() + ", ownerId=" + getOwnerId() + "]";
    }
}
