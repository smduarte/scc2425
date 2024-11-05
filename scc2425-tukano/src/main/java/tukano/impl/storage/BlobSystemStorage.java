package main.java.tukano.impl.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.BlobClient;  // For BlobClient
import java.io.ByteArrayInputStream;      // For ByteArrayInputStream
import java.util.function.Consumer;
import java.util.logging.Logger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import main.java.tukano.api.Result;
import main.java.tukano.impl.JavaUsers;
import main.java.utils.Hash;

public class BlobSystemStorage implements BlobStorage {
    String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=st7066270663;AccountKey=BQp5y2hpOFd6oDJRIpt4C/i0hMnGnVDwTbqWqHg2gMFuInxxWkQ2YomHeGmIluf/ZA5v+2F4AMlM+AStg9AZyQ==;EndpointSuffix=core.windows.net";
    String containerName = "blobs";

    private BlobContainerClient containerClient;

    public BlobSystemStorage() {
        // Initialize BlocContainerCLient
        containerClient = new BlobContainerClientBuilder()
                .connectionString(storageConnectionString)
                .containerName(containerName)
                .buildClient();
        Map<String, byte[]> map = new HashMap<>();
    }

    @Override
    public Result<Void> write(String path, byte[] bytes) {
        BlobClient blobClient = containerClient.getBlobClient(path);
        blobClient.upload(new ByteArrayInputStream(bytes), bytes.length, true);
        return Result.ok();
    }

    @Override
    public Result<Void> delete(String path) {
        try {
            // Get the BlobClient for the specific blob
            BlobClient blobClient = containerClient.getBlobClient(path);

            // Check if the blob exists before trying to delete it
            if (blobClient.exists()) {
                // Delete the blob
                blobClient.delete();
                return Result.ok();  // Return success if deletion is successful
            } else {
                throw  new WebApplicationException(Status.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("EXCEPTION: " + e);
            throw  new WebApplicationException(Status.CONFLICT);
        }
    }

    public Result<Void> deleteAllBlobsInPath(String path) {
        try {
            for (BlobItem blobItem : containerClient.listBlobsByHierarchy(path)) {
                BlobClient blobClient = containerClient.getBlobClient(blobItem.getName());
                blobClient.delete();
            }
            return Result.ok();
        } catch (Exception e) {
            System.err.println("EXCEPTION: " + e);
            throw new WebApplicationException(Status.CONFLICT);
        }
    }

    @Override
    public Result<byte[]> read(String path) {
        return Result.ok(download(path));
    }

    @Override
    public Result<Void> read(String path, Consumer<byte[]> sink) {
        downloadSink(path, sink);
        return Result.ok();
    }


    private byte[] download(String id) {
        try {
            return containerClient.getBlobClient(id).downloadContent().toBytes();  // Download blob from Azure
        } catch (Exception e) {
            throw new WebApplicationException(Status.NOT_FOUND);  // Handle if the blob does not exist
        }
    }

    private byte[] downloadSink (String id, Consumer<byte[]> sink) {
        try {
            return containerClient.getBlobClient(id).downloadContent().toBytes();  // Download blob from Azure
        } catch (Exception e) {
            throw new WebApplicationException(Status.NOT_FOUND);  // Handle if the blob does not exist
        }
    }

    private List<String> list() {
        List<String> blobNames = new ArrayList<>();
        for (BlobItem blobItem : containerClient.listBlobs()) {
            blobNames.add(blobItem.getName());
        }
        return blobNames;
    }
}