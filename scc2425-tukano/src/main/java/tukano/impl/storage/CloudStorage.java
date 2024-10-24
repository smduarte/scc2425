package tukano.impl.storage;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import tukano.api.result.Result;

public class CloudStorage implements BlobStorage {
    // Get connection string in the storage access keys page
    // mvn -DconnectionString=some_value install
    private final String storageConnectionString = System.getenv("connectionString");
    private static final String CONTAINER_NAME = "images";
    private final BlobContainerClient containerClient = new BlobContainerClientBuilder()
            .connectionString(storageConnectionString)
            .containerName(CONTAINER_NAME)
            .buildClient();

    @Override
    public Result<Void> write(String path, byte[] bytes) {
        var blobPath = containerClient.getBlobClient(path);
        blobPath.upload(BinaryData.fromBytes(bytes));
        return Result.ok(null);
    }

    @Override
    public Result<Void> delete(String path) {
        var blob = containerClient.getBlobClient(path);
        blob.delete();
        return Result.ok(null);
    }


    @Override
    public Result<byte[]> read(String path) {
        var blob = containerClient.getBlobClient(path);
        return Result.ok(blob.downloadContent().toBytes());
    }

    @Override
    public Result<Void> read(String path, Consumer<byte[]> sink) {
        var blob = containerClient.getBlobClient(path);
        sink.accept(blob.downloadContent().toBytes());
        return Result.ok(null);
    }
}
