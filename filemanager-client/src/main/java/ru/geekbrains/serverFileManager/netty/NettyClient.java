package ru.geekbrains.serverFileManager.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {
    private SocketChannel channel;
    public NettyClient() {
        Thread t = new Thread(() -> {
            EventLoopGroup workGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new FileInfoToBytesEncoder());
                            }
                        });
                ChannelFuture future = b.connect("localhost", 8189).sync();
                this.channel = (SocketChannel) future.channel();
                future.channel().closeFuture().sync();
            } catch (Exception e) {

            } finally {
                workGroup.shutdownGracefully();
            }

        });
        t.setDaemon(true);
        t.start();
    }

    public void sendMessage(Object msg) {
        channel.writeAndFlush(msg);
    }
}
