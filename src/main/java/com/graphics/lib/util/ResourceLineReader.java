package com.graphics.lib.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.google.common.base.Charsets;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Read lines from a resource
 * 
 * @author paul.brandon
 * @version 1.0
 */
public class ResourceLineReader {
    
    private ResourceLineReader() {}
    
    /**
     * Returns an Observable that emits lines from the resource file being read
     * 
     * @param sourcefile
     * @return
     */
    public static Observable<String> getLineObserver(String resource) {
        
        return Observable.create(emitter -> {
                
                final Cancelled cancelled = new Cancelled();
                emitter.setCancellable(cancelled::cancel);
                fetchAndLines(resource, emitter, cancelled);
                emitter.onComplete();
        });

    }

    private static void fetchAndLines(final String resource, final ObservableEmitter<String> emitter, Cancelled cancelled) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(ResourceLineReader.class.getClassLoader().getResourceAsStream(resource), Charsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null && !cancelled.isCancelled) {
                emitter.onNext(line);
            }
        } catch (Exception e) {
            emitter.onError(e);
        }
    }
    
    private static class Cancelled {
        boolean isCancelled = false;
        
        public void cancel() {
            isCancelled = true;
        }
    }
}
