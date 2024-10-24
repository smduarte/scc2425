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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import main.java.tukano.api.Result;
import main.java.utils.Hash;

public class BlobSystemStorage implements BlobStorage {
    String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=scc212270663;AccountKey=N3VETIuitF5wTw2c2NHFiJnXSvuQqOLT68mXinwolfTRezxqY5VntPdt0e3zh0dO7LYB4NgOUxEO+ASt9HZDZQ==;EndpointSuffix=core.windows.net";
    String containerName = "images";

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
        upload(bytes);
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
            throw  new WebApplicationException(Status.CONFLICT);
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

    private String upload(byte[] contents) {
        var key = Hash.of(contents);
        //	map.put(key, contents);
        BlobClient blob = containerClient.getBlobClient(key); // Get blob client
        blob.upload(new ByteArrayInputStream(contents), contents.length); // Upload blob to Azure
        return key;
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