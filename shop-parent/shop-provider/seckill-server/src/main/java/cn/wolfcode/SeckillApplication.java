package cn.wolfcode;

import io.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/* 启用 Spring 缓存模块 */
@EnableCaching
@SpringBootApplication(exclude = {SeataAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan(basePackages = "cn.wolfcode.mapper")
public class SeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }
}
