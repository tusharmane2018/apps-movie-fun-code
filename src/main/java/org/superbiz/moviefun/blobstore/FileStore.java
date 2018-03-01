package org.superbiz.moviefun.blobstore;

import org.apache.tika.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.Long.parseLong;
import static java.lang.String.format;

@Component
public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        saveUploadToFile(blob.inputStream, getCoverFile(parseLong(blob.name)));
    }

    @Override
    public Optional<Blob> get(String albumId) throws IOException, URISyntaxException {
        Path coverFilePath = getExistingCoverPath(parseLong(albumId));
        File file = coverFilePath.toFile();
        return file != null ?
                Optional.of(new Blob(albumId, new FileInputStream(file), Files.probeContentType(coverFilePath))) :
                Optional.empty();
    }

    @Override
    public void deleteAll() {
        // ...
    }

    private void saveUploadToFile(InputStream inputStream, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    private File getCoverFile(long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }


}