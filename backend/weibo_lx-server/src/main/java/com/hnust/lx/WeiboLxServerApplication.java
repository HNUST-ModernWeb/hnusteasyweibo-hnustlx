package com.hnust.lx;

import com.hnust.lx.properties.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class WeiboLxServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeiboLxServerApplication.class, args);
    }

}
