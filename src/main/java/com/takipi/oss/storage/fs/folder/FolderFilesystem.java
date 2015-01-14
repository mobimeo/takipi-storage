package com.takipi.oss.storage.fs.folder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;

public class FolderFilesystem implements Filesystem {
    private final File root;
    private final double maxUsedStoragePercentage;

    public FolderFilesystem(String rootFolder, double maxUsedStoragePercentage) {
        this.root = new File(rootFolder);
        this.maxUsedStoragePercentage = maxUsedStoragePercentage;

        if (!healthy()) {
            throw new IllegalStateException("Problem with path " + rootFolder);
        }
    }

    @Override
    public boolean healthy() {
        return (folderCheck() && maxUsedStorageCheck());
    }

    private boolean folderCheck() {
        return ((this.root.canRead()) && (this.root.canWrite()));
    }

    private boolean maxUsedStorageCheck() {
        return ((maxUsedStoragePercentage >= 0) && (maxUsedStoragePercentage < 1) && ((this.root.getUsableSpace() / this.root
                .getTotalSpace()) <= maxUsedStoragePercentage));
    }

    @Override
    public InputStream getRecord(Record record) throws IOException {
        File file = new File(buildPath(record));

        return new FileInputStream(file);
    }

    @Override
    public void putRecord(Record record, InputStream is) throws IOException {
        File file = new File(buildPath(record));

        beforePut(file);

        OutputStream os = new FileOutputStream(file);

        IOUtils.copy(is, os);

        os.flush();
    }

    @Override
    public String getJson(String key) throws IOException {
        File file = new File(buildPath(key));

        return FileUtils.readFileToString(file);
    }

    @Override
    public void putJson(String key, String string) throws IOException {
        File file = new File(buildPath(key));

        beforePut(file);

        FileUtils.writeStringToFile(file, string);
    }

    @Override
    public void delete(Record record) throws IOException {
        File file = new File(buildPath(record));

        file.delete();
    }

    protected String buildPath(Record record) {
        Path recordPath = Paths.get(root.getPath(), escape(record.getServiceId()), escape(record.getType()),
                escape(record.getServiceId()));

        return recordPath.toString();
    }

    protected String buildPath(String key) {
        Path recordPath = Paths.get(root.getPath(), escape(key));

        return recordPath.toString();
    }

    protected void beforePut(File file) {
        file.getParentFile().mkdirs();
    }

    protected String escape(String value) {
        return value.replace("..", "__").replace("/", "-").replace("\\", "-");
    }
}
