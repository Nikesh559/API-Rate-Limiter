package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@CrossOrigin(origins = "*")
@RestController
public class Controller {

    private RateLimiter rateLimiter;

    @Autowired
    private RateLimiterFactory factory;

    @GetMapping("/api")
    public ResponseEntity testRateLimiter(HttpServletRequest request) {
        if(rateLimiter.permitRequest(request)) {
            return new ResponseEntity(HttpStatus.OK);
        }
        else
            return new ResponseEntity(HttpStatus.TOO_MANY_REQUESTS);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/algorithm")
    public ResponseEntity setupRateLimiter(@RequestBody HashMap<String, Integer> hm) {
        System.out.println("Request" +hm);
        int type = hm.get("type");
        int bucketSize = hm.get("bucketSize");
        int refillRate = hm.get("refillRate");
        rateLimiter = factory.getRateLimiter(type, bucketSize, refillRate);
        return new ResponseEntity(HttpStatus.OK);
    }
}
