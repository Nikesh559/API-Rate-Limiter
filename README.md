# API Rate Limiter

API rate Limiting is basically limiting the access to api based on certain factors. It is applied for both security of system and to maintain quality of service(QoS). A malicious user can potentially bring entire system down by sending tonnes of request.
Rate limiter prevent this by throttling the request when limit is exceeded.

# Low Level Design in Java
Below code explain of core classes and interfaces used to design the system.

<b>RateLimiter Interface</b> consists of single method with boolean return type. If method returns true, request is fullfilled else it is rejected. <br>
 

```` Java

public interface RateLimiter {
    boolean permitRequest(HttpServletRequest request);
}
`````
<br>

<b>RateLimiterFactory Factory Method</b> to return type of rate limiting algorithm. <br>


```` Java

public class RateLimiterFactory {

    public static RateLimiter getRateLimiter(int type, int param1, int param2) {
        RateLimiter rateLimiter = null;
        
        switch (type) {
            case 1 : rateLimiter = new TokenBucket(param1, param2);
            break;

            case 2 : rateLimiter = new SlidingWindow(param1, param2);
            break;
        }
        return rateLimiter;
    }
}
`````
<br>

<b> Token Bucket Algorithm </b> implementation

````` Java
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

````````````````
<br>

<b> Sliding Window Algorithm </b> implementation

```` Java
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
`````

