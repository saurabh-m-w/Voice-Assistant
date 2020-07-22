package edu.cmu.pocketsphinx;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

public class Assets {
    public static final String ASSET_LIST_NAME = "assets.lst";
    public static final String HASH_EXT = ".md5";
    public static final String SYNC_DIR = "sync";
    protected static final String TAG = Assets.class.getSimpleName();
    private final AssetManager assetManager;
    private final File externalDir;

    public Assets(Context context) throws IOException {
        File appDir = context.getExternalFilesDir(null);
        if (appDir == null) {
            throw new IOException("cannot get external files dir, external storage state is " + Environment.getExternalStorageState());
        }
        this.externalDir = new File(appDir, SYNC_DIR);
        this.assetManager = context.getAssets();
    }

    public Assets(Context context, String dest) {
        this.externalDir = new File(dest);
        this.assetManager = context.getAssets();
    }

    public File getExternalDir() {
        return this.externalDir;
    }

    public Map<String, String> getItems() throws IOException {
        Map<String, String> items = new HashMap<>();
        for (String path : readLines(openAsset(ASSET_LIST_NAME))) {
            items.put(path, new BufferedReader(new InputStreamReader(openAsset(path + HASH_EXT))).readLine());
        }
        return items;
    }

    public Map<String, String> getExternalItems() {
        try {
            Map<String, String> items = new HashMap<>();
            for (String line : readLines(new FileInputStream(new File(this.externalDir, ASSET_LIST_NAME)))) {
                String[] fields = line.split(" ");
                items.put(fields[0], fields[1]);
            }
            return items;
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    public Collection<String> getItemsToCopy(String path) throws IOException {
        Collection<String> items = new ArrayList<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.offer(path);
        while (!queue.isEmpty()) {
            String path2 = (String) queue.poll();
            String[] list = this.assetManager.list(path2);
            for (String nested : list) {
                queue.offer(nested);
            }
            if (list.length == 0) {
                items.add(path2);
            }
        }
        return items;
    }

    private List<String> readLines(InputStream source) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(source));
        while (true) {
            String line = br.readLine();
            if (line == null) {
                return lines;
            }
            lines.add(line);
        }
    }

    private InputStream openAsset(String asset) throws IOException {
        return this.assetManager.open(new File(SYNC_DIR, asset).getPath());
    }

    public void updateItemList(Map<String, String> items) throws IOException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(new File(this.externalDir, ASSET_LIST_NAME)));
        for (Entry<String, String> entry : items.entrySet()) {
            pw.format("%s %s\n", new Object[]{entry.getKey(), entry.getValue()});
        }
        pw.close();
    }

    public File copy(String asset) throws IOException {
        InputStream source = openAsset(asset);
        File destinationFile = new File(this.externalDir, asset);
        destinationFile.getParentFile().mkdirs();
        OutputStream destination = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[1024];
        while (true) {
            int nread = source.read(buffer);
            if (nread == -1) {
                break;
            } else if (nread == 0) {
                int nread2 = source.read();
                if (nread2 < 0) {
                    break;
                }
                destination.write(nread2);
            } else {
                destination.write(buffer, 0, nread);
            }
        }
        destination.close();
        return destinationFile;
    }

    public File syncAssets() throws IOException {
        Collection<String> newItems = new ArrayList<>();
        Collection<String> unusedItems = new ArrayList<>();
        Map<String, String> items = getItems();
        Map<String, String> externalItems = getExternalItems();
        for (String path : items.keySet()) {
            if (!((String) items.get(path)).equals(externalItems.get(path)) || !new File(this.externalDir, path).exists()) {
                newItems.add(path);
            } else {
                Log.i(TAG, String.format("Skipping asset %s: checksums are equal", new Object[]{path}));
            }
        }
        unusedItems.addAll(externalItems.keySet());
        unusedItems.removeAll(items.keySet());
        for (String path2 : newItems) {
            File file = copy(path2);
            Log.i(TAG, String.format("Copying asset %s to %s", new Object[]{path2, file}));
        }
        for (String path3 : unusedItems) {
            File file2 = new File(this.externalDir, path3);
            file2.delete();
            Log.i(TAG, String.format("Removing asset %s", new Object[]{file2}));
        }
        updateItemList(items);
        return this.externalDir;
    }
}
