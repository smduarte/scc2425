package main.java.tukano.impl.data;

import jakarta.persistence.Entity;

@Entity
public class FollowingCosmos extends Following{
    private String id;

    public FollowingCosmos() {
        super();
    }

    public FollowingCosmos(Following f) {
        super(f.follower, f.followee);
        this.id = f.follower+f.followee;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "FollowingCosmos [id=" + id + ", follower=" + getFollower() + ", followee=" + getFollowee() + "]";
    }
}
