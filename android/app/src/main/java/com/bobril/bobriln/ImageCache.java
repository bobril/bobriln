package com.bobril.bobriln;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ImageCache implements Runnable {
    private final Thread thread;
    LruCache<List<Object>, Bitmap> cache;
    ArrayBlockingQueue<List<Object>> loadQueue = new ArrayBlockingQueue<List<Object>>(64);
    Bitmap loadingTag = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    float density;
    private GlobalApp _app;

    ImageCache(GlobalApp app) {
        _app = app;
        cache = new LruCache<List<Object>, Bitmap>(10 * 1024 * 1204) {
            @Override
            protected int sizeOf(List<Object> key, Bitmap value) {
                return 200 + value.getByteCount();
            }
        };
        thread = new Thread(this);
        thread.start();
    }

    public void setDensity(float density) {
        if (this.density != density) {
            this.density = density;
            cache.evictAll();
            loadQueue.clear();
        }
    }

    public Bitmap get(List<Object> params) {
        Bitmap res = cache.get(params);
        if (res == loadingTag) return null;
        if (res == null) {
            if (loadQueue.offer(params)) {
                cache.put(params, loadingTag);
            }
            return null;
        }
        return res;
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<Object> param = loadQueue.poll(1, TimeUnit.MINUTES);
                if (param == null) continue;
                if (param.size() < 3) {
                    cache.put(param, Bitmap.createBitmap(Math.max(1, Math.round(FloatUtils.unboxToFloat(param.get(0)))),
                            Math.max(1, Math.round(FloatUtils.unboxToFloat(param.get(1)))), Bitmap.Config.ARGB_8888));
                    updated();
                    continue;
                }
                int idx = 2;
                while (idx < param.size() - 2) {
                    double den = FloatUtils.unboxToFloat(param.get(idx));
                    if (den == this.density) break;
                    if (den > this.density * 1.3f) break;
                    idx += 2;
                }
                InputStream input = _app.loadContent((String) param.get(idx + 1));
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                cache.put(param, bitmap);
                updated();
            } catch (InterruptedException e) {
                return;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void updated() {
    }

}
