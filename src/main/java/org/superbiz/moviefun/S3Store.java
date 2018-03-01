package org.superbiz.moviefun;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.tika.Tika;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;


public class S3Store implements BlobStore {
    private AmazonS3 s3Client ;
    private String s3BucketName  ;

    public S3Store(AmazonS3Client s3Client, String s3BucketName) {
        this.s3Client = s3Client;
        this.s3BucketName= s3BucketName;
    }

    @Override
    public void put(Blob blob) throws IOException {
        s3Client.putObject(s3BucketName,blob.name,blob.inputStream,null);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException, URISyntaxException {
        S3Object object = s3Client.getObject(s3BucketName, name);

        return Optional.ofNullable(new Blob(object.getKey(),object.getObjectContent(), new Tika().detect(object.getObjectContent())));
    }

    @Override
    public void deleteAll() {

    }
}
