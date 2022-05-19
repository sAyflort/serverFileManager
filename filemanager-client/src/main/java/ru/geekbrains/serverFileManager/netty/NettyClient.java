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
import ru.commons.SendFileRequest;

import java.util.ArrayList;
import java.util.List;

import static ru.commons.Commands.*;


public class NettyClient {
    private final int MB_20 = 20 * 1_000_000;
    private final int SHORT_MB_20 = MB_20 - 500_000;
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
            List<FileInfo> listFiles = cutFile(msg);
            channel.writeAndFlush(new SendFileRequest(SEND_FILE, log, pass, null, listFiles.get(0)));
            if(listFiles.size() > 1) {
                for (int i = 1; i < listFiles.size(); i++) {
                    channel.writeAndFlush(new SendFileRequest(PART_FILE, log, pass, null, listFiles.get(i)));
                }
            }
        }
        if(command == GET_FILE) {
            channel.writeAndFlush(new SendFileRequest(GET_FILE, log, pass, null , msg));
        }

    }

    public void sendMsg(Commands commands, String log, String pass, String path, FileInfo fileInfo) {
        LOGGER.info("Отправлен запрос: "+commands);
        channel.writeAndFlush(new SendFileRequest(commands , log, pass, path, fileInfo));

    }

    public List<FileInfo> cutFile(FileInfo fileInfo) {
        List<FileInfo> listFiles = new ArrayList<>();

        long size = fileInfo.getSize();
        int numberCut = (int) (size/SHORT_MB_20)+1;
        byte[] bytesFile = fileInfo.getFile();

        for (int i = 0; i < numberCut; i++) {
            byte[] bytes = new byte[i == (numberCut-1) ? (bytesFile.length-i*SHORT_MB_20) : SHORT_MB_20];
            System.arraycopy(bytesFile, i*SHORT_MB_20, bytes, 0, bytes.length);
            listFiles.add(new FileInfo(
                    fileInfo.getFileName(),
                    fileInfo.getType(),
                    fileInfo.getSize(),
                    bytes
                    ));
        }

        return listFiles;
    }
}
