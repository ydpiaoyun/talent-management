package com.talent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 人才管理系统启动类
 * <p>
 * Spring Boot 应用入口，启动后监听 8088 端口。
 * </p>
 *
 * @author talent-hr
 */
@SpringBootApplication
public class TalentApplication {

    /**
     * 主方法，应用入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TalentApplication.class, args);
    }
}
