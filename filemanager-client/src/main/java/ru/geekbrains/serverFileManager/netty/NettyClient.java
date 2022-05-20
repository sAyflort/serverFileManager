package ru.geekbrains.serverFileManager.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.commons.Commands;
import ru.commons.FileInfo;
import ru.commons.FileSplit;
import ru.commons.SendFileRequest;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static ru.commons.Commands.*;


public class NettyClient {
    private final int MB_20 = 20 * 1_000_000;
    private SocketChannel channel;

    private static final Logger LOGGER = LogManager.getLogger(NettyClient.class);
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
                                ch.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(MB_20, ClassResolvers.cacheDisabled(null)),
                                        new ClientHandler()
                                );
                            }
                        });
                ChannelFuture future = b.connect("localhost", 8189).sync();
                this.channel = (SocketChannel) future.channel();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                LOGGER.warn(e.getMessage());
            } finally {
                workGroup.shutdownGracefully();
            }

        });
        t.setDaemon(true);
        t.start();
    }

    public void sendFile(FileInfo msg, String log, String pass, Commands command) {
        LOGGER.info(command);
        if (command == SEND_FILE) {
            AtomicBoolean isBeginFile = new AtomicBoolean(true);
            Consumer<byte[]> filePartResponse = bytes -> {
                SendFileRequest sendFileRequest;
                if(isBeginFile.get()) {
                    sendFileRequest = new SendFileRequest(SEND_FILE, log, pass, null, new FileInfo(msg, bytes));
                    isBeginFile.set(false);
                } else {
                    sendFileRequest = new SendFileRequest(PART_FILE, log, pass, null, new FileInfo(msg, bytes));
                }
                channel.writeAndFlush(sendFileRequest);
            };
            FileSplit fileSplit = new FileSplit();
            fileSplit.split(Paths.get(msg.getFilePath()), filePartResponse);
        }
        if(command == GET_FILE) {
            channel.writeAndFlush(new SendFileRequest(GET_FILE, log, pass, null , msg));
        }
    }

    public void sendMsg(Commands commands, String log, String pass, String path, FileInfo fileInfo) {
        LOGGER.info("Отправлен запрос: " + commands);
        channel.writeAndFlush(new SendFileRequest(commands , log, pass, path, fileInfo));

    }
}
