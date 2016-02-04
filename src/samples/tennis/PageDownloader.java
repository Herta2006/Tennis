package samples.tennis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

class PageDownloader implements Callable<byte[]> {
    private String pageUrl;

    public PageDownloader(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    @Override
    public byte[] call() throws Exception {
        return downloadPage();
    }

    private byte[] downloadPage() {
        byte[] bytes = null;
        URL url;
        try {
            url = new URL(pageUrl);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = inputStream.read(buf)) >= 0) {
                outputStream.write(buf, 0, n);
            }
            bytes = outputStream.toByteArray();
        } catch (MalformedURLException e) {
            Logger.getGlobal().log(Level.WARNING, "cannot create a URL by string " + pageUrl);
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, "cannot open connection/get input stream/read bytes by url " + pageUrl);
        }
        return bytes;
    }
}