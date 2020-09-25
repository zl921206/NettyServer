package com.netty;

import com.netty.server.NettyHttpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * NettyServer启动类
 */
@Slf4j
public class NettyServerStart {

    public static void main(String[] args) {
        // 初始化Spring上下文
        new ClassPathXmlApplicationContext("applicationContext.xml");
        // 启动NettyServer
        new NettyHttpServer(8080).start();
    }

}
