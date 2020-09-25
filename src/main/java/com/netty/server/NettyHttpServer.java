package com.netty.server;

import com.netty.context.ClassPathApplicationContext;
import com.netty.servlet.DispatcherServlet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * NettyServer启动配置类
 */
@Slf4j
public class NettyHttpServer {

    /**
     * 端口号
     */
    private int port;

    /**
     * 一个参数的构造方法
     *
     * @param port
     */
    public NettyHttpServer(Integer port) {
        this.port = port;
    }

    /**
     * 启动方法
     */
    public void start() {
        //  负责接收客户端的连接的线程。线程数设置为1即可，netty处理链接事件默认为单线程，过度设置反而浪费cpu资源
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //  负责处理数据传输的工作线程。线程数默认为CPU核心数乘以2
        EventLoopGroup workerGroup = new NioEventLoopGroup(8);
        try {
            // 创建服务启动程序
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup) // 添加组
                    .channel(NioServerSocketChannel.class)  // 使用通道
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addFirst(new IdleStateHandler(0L, 0L, 64L, TimeUnit.SECONDS));
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketServerCompressionHandler());
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new DispatcherServlet());
                        }
                    })
                    //标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //Netty4使用对象池，重用缓冲区
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    //是否启用心跳保活机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //禁止使用Nagle算法，便于小数据即时传输
                    .childOption(ChannelOption.TCP_NODELAY, true);
            /**
             * 指定服务器启动需要使用的属性文件，属性文件放在classpath目录下，多个文件以英文逗号隔开
             * 属性文件可以配置controller扫描包的包路径以及，统一的异常处理器
             */
            ClassPathApplicationContext.getInstance().start("web.properties");
            // 绑定端口
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            // 开启监听
            channelFuture.addListener(f -> {
                if (f.isSuccess()) {
                    log.info("NettyHttpServer Run Successfully! Port: " + port);
                } else {
                    log.error("NettyHttpServer Run failed!");
                }
            });
            //等待服务监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("NettyHttpServer start fail! cause：{}", e.getMessage());
        } finally {
            //释放资源
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
