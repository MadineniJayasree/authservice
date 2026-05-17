package com.jayasree.authservice.controller;

import com.jayasree.authservice.dto.AuthRequest;
import com.jayasree.authservice.util.JwtUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.concurrent.CompletableFuture;

@RestController
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/auth/login")
    public String login(@RequestBody AuthRequest request) {

        if ("admin".equals(request.getUsername())
                && "password".equals(request.getPassword())) {

            return jwtUtil.generateToken(
                    request.getUsername(),
                    "ADMIN"
            );
        }

        if ("user".equals(request.getUsername())
                && "password".equals(request.getPassword())) {

            return jwtUtil.generateToken(
                    request.getUsername(),
                    "USER"
            );
        }

        return "Invalid Credentials";
    }

    @GetMapping("/admin")
    public String adminApi() {

        return "Admin API Accessed";
    }
    @GetMapping("/external-api")
    @CircuitBreaker(name="externalService",fallbackMethod="fallbackResponse")
    @Retry(name="externalService",fallbackMethod = "fallbackResponse")
    @TimeLimiter(name="externalService")
    public CompletableFuture<String> externalApi(){
        return CompletableFuture.supplyAsync(()->{
            try{
                    Thread.sleep(10000);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return "Success";
        });
    }
    public CompletableFuture<String> fallbackResponse(
            Exception ex) {

        return CompletableFuture.completedFuture(
                "Fallback Response : Timeout occurred"
        );
    }
    @GetMapping("/ratelimit")
    @RateLimiter(
            name = "ratelimiterApi",
            fallbackMethod = "ratelimiterFallback"
    )
    public String ratelimitApi() {

        return "API access Successful";
    }

    public String ratelimiterFallback(Exception ex) {

        return "Too many requests. Try again later.";
    }
}