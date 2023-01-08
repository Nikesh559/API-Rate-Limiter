package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterFactory {

    @Autowired
    private RedisTemplate redisTemplate;

    public RateLimiter getRateLimiter(int type, int bucketSize, int time) {
        switch (type) {
            case 1 : TokenBucket rateLimiter = new TokenBucket(bucketSize, time);
            rateLimiter.setRedisTemplate(redisTemplate);
            return rateLimiter;

            case 2 : SlidingWindow slidingWindow = new SlidingWindow(bucketSize, time);
            slidingWindow.setRedisTemplate(redisTemplate);
            return slidingWindow;
        }
        return null;
    }
}
