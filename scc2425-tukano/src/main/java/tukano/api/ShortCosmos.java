package main.java.tukano.api;

import jakarta.persistence.Entity;
import main.java.tukano.impl.Token;

@Entity
public class ShortCosmos extends Short {

    private String id; // This will be the id field for Cosmos DB

    public ShortCosmos() {
        super(); // Call the superclass constructor
    }

    public ShortCosmos(String shortId, String ownerId, String blobUrl, long timestamp, int totalLikes) {
        super(shortId, ownerId, blobUrl, timestamp, totalLikes); // Call the superclass constructor
        this.id = shortId;
    }
    public ShortCosmos(Short shrt) {
        super(shrt.shortId, shrt.ownerId, shrt.blobUrl, shrt.timestamp, shrt.totalLikes);
        this.id = shrt.shortId; // Set the id as shortId
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ShortCosmos [id=" + id + ", shortId=" + getShortId() + ", ownerId=" + getOwnerId() +
                ", blobUrl=" + getBlobUrl() + ", timestamp=" + getTimestamp() +
                ", totalLikes=" + getTotalLikes() + "]";
    }

    public ShortCosmos copyWithLikesAndToken(long totLikes) {
        var urlWithToken = String.format("%s?token=%s", getBlobUrl(), Token.get(getBlobUrl()));
        return new ShortCosmos(getShortId(), getOwnerId(), urlWithToken, getTimestamp(), (int) totLikes);
    }
}
