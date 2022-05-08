package ru.geekbrains.cloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.commons.Request;
import ru.geekbrains.cloud.dataBaseService.BaseAuthService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static ru.commons.Commands.*;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final String basePath  = "C:\\Users\\sAyflort\\IdeaProjects\\serverFileManager\\cloud\\";
    private BaseAuthService baseAuthService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.baseAuthService = ServerApp.getBaseAuthService();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof Request) {
            Request request = (Request) msg;

            if(baseAuthService.isAuth(request.getLogin(), request.getPass())) {
                if(request.getType() == AUTH) {
                    ctx.channel().writeAndFlush(AUTH_OK);
                    return;
                }
                if(request.getType() == SEND_FILE) {
                    String path = basePath+request.getLogin();
                    Files.createDirectories(Path.of(path));
                    path += "\\"+request.getFileInfo().getFileName();

                    Files.deleteIfExists(Path.of(path));
                    Files.createFile(Path.of(path));
                    Files.write(Path.of(path),
                            request.getFileInfo().getFile(), StandardOpenOption.APPEND);
                }
                if(request.getType() == PART_FILE) {
                    String path = basePath+request.getLogin()+"\\"+request.getFileInfo().getFileName();
                    Files.write(Path.of(path),
                            request.getFileInfo().getFile(), StandardOpenOption.APPEND);
                }
            } else {
                return;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
