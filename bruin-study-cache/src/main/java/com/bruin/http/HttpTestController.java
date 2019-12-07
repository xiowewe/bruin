package com.bruin.http;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: xiongwenwen   2019/11/27 10:37
 */
@RequestMapping("bruin")
@RestController
public class HttpTestController {

    @RequestMapping("cache")
    public ResponseEntity<String> cache(@RequestHeader(value = "If-Modified-Since", required = false) Date ifModifiedSince) throws ExecutionException {
        DateFormat format = new SimpleDateFormat("EEE, d MMM YYYY HH:mm:ss 'GMT'", Locale.US);

        long lastModifieMills = getLastMOdified() /1000 * 1000;
        long now = System.currentTimeMillis() / 1000 * 1000;
        long maxAge = 5;

        if(ifModifiedSince != null && ifModifiedSince.getTime() == lastModifieMills){
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add("Date", format.format(new Date(now)));
            headers.add("Expires", format.format(new Date(now + maxAge * 1000)));
            headers.add("Cache-Controller", "max-age=" + maxAge);

            return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
        }

        String body = "<a href=''> 点击访问当前链接</a>";
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Date", format.format(new Date(now)));
        headers.add("Last-Modified", format.format(new Date(lastModifieMills)));
        headers.add("Expires", format.format(new Date(now + maxAge * 1000)));
        headers.add("Cache-Controller", "max-age=" + maxAge);

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }




    Cache<String, Long> lastModifiedCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    public long getLastMOdified() throws ExecutionException {
        return lastModifiedCache.get("lastModified", () ->{
            return System.currentTimeMillis();
        });
    }
}
