package demo;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.http.HttpServletRequest;

public class TokenBucket implements RateLimiter{

    private RedisTemplate<String, Object> redisTemplate;
    private int refillRateInSecs;
    private int bucketSize;
    private final String LAST_REFILL_TIME = "lastRefillTime";
    private final String TOKEN_CNT = "tokenCount";

    public TokenBucket(int bucketSize, int refillRateInSecs) {
        if(bucketSize <= 0 && refillRateInSecs <= 0)
            throw new IllegalArgumentException("Bucket Size and Refill Rate Must greater than zero");

        this.bucketSize = bucketSize;
        this.refillRateInSecs = refillRateInSecs;
    }

    @Override
    public synchronized boolean permitRequest(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        HashOperations<String, Object, Object> tokenSets = redisTemplate.opsForHash();
        HashOperations<String, Object, Object> refillSets = redisTemplate.opsForHash();
        boolean isMember = tokenSets.hasKey(TOKEN_CNT, ip);

        if(isMember) {
           long lastRefillTimeInMillis = (Long) refillSets.get(LAST_REFILL_TIME, ip);
           long currTimeInMillis = System.currentTimeMillis();
            System.out.println(ip+" ->"+lastRefillTimeInMillis+" "+currTimeInMillis+" "+(currTimeInMillis - lastRefillTimeInMillis));
           if(currTimeInMillis - lastRefillTimeInMillis >= refillRateInSecs * 1000) {
               // Refill the Bucket
               refillSets.put(LAST_REFILL_TIME, ip, currTimeInMillis);
               tokenSets.put(TOKEN_CNT, ip, bucketSize - 1);
               return true;
           }
           else {
               int tokensLeft = (Integer) tokenSets.get(TOKEN_CNT, ip);
               if (tokensLeft > 0) {
                   tokenSets.put(TOKEN_CNT, ip, tokensLeft - 1);
                   return true;
               }
               else return false;
           }
        }
        else {
            refillSets.put(LAST_REFILL_TIME, ip, System.currentTimeMillis());
            tokenSets.put(TOKEN_CNT, ip, bucketSize - 1);
            return true;
        }
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
