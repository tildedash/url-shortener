package controllers;

import org.apache.commons.lang.RandomStringUtils;
import play.*;
import play.mvc.*;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;

public class Application extends Controller {

    static final JedisShardInfo redisConfig = new JedisShardInfo(
            Play.configuration.getProperty("redis.host", "localhost"),
            Integer.valueOf(Play.configuration.getProperty("redis.port", "6379")));

    static {
        redisConfig.setPassword(Play.configuration.getProperty("redis.password", "foobared"));
    }

    public static void index() {
        render();
    }

    public static void getUrl(String key) {
        Jedis jedis = new Jedis(redisConfig);
        String redirectUrl = jedis.get("url#" + key);
        if (redirectUrl == null) {
            notFound();
        }
        redirect(redirectUrl);
    }

    public static String postUrl(String url) {
        Jedis jedis = new Jedis(redisConfig);
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        int size = 1;
        String key = null, exitingUrl = null;
        do {
            key = RandomStringUtils.random(size, letters);
            exitingUrl = findUrl(key);
            size++;
        } while (exitingUrl != null);

        String niceUrl = url.startsWith("http://") || url.startsWith("https://") ? url : "http://" + url;
        jedis.set("url#" + key, niceUrl);
        return key;
    }

    private static String findUrl(String key) {
        Jedis jedis = new Jedis(redisConfig);
        return jedis.get("url#" + key);
    }
}