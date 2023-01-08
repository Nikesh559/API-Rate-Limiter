package demo;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class SlidingWindow implements RateLimiter{

    private RedisTemplate<String, Object> redisTemplate;
    private int windowSizeInSecs;
    private int bucketSize;
    private final String WINDOW = "window";
    private final String TOKEN_CNT = "tokenCount";

    public SlidingWindow(int bucketSize, int windowSizeInSecs) {
        this.windowSizeInSecs = windowSizeInSecs;
        this.bucketSize = bucketSize;
    }

    @Override
    public synchronized boolean permitRequest(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        ListOperations<String, Object> list = redisTemplate.opsForList();
        String listName = WINDOW + ip;
        long currTimestamp = System.currentTimeMillis();

        try {
            while (currTimestamp - (Long) list.index(listName, 0) >= windowSizeInSecs * 1000) {
                list.leftPop(listName);
            }
        }catch (Exception exception) {

        }
        if(list.size(listName) < bucketSize) {
            list.rightPush(listName, currTimestamp);
            return true;
        }
        else
            return false;
    }


    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
