package ru.geekbrains.cloud;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.commons.SendFileRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import ru.geekbrains.cloud.dataBaseService.BaseAuthService;

public class ServerApp {
    private static BaseAuthService baseAuthService = new BaseAuthService();
    private static final int MB_20 = 20 * 1_000_000;

    private static final Logger LOGGER = LogManager.getLogger(ServerApp.class);

    public static void main(String[] args) {
        LOGGER.info("Запуск сервера");
        EventLoopGroup bossGroup = new NioEventLoopGroup(4);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(MB_20,ClassResolvers.cacheDisabled(SendFileRequest.class.getClassLoader())),
                                    new ServerHandler()
                            );
                        }
                    });

            ChannelFuture future = b.bind(8189).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            LOGGER.info("Завершение работы");
        }
    }

    public static BaseAuthService getBaseAuthService() {
        return baseAuthService;
    }
}
