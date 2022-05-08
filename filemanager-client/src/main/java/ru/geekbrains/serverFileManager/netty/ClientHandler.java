package ru.geekbrains.serverFileManager.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.commons.Commands;
import ru.geekbrains.serverFileManager.Controller;

import static ru.commons.Commands.AUTH_OK;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Controller controller;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.controller = Controller.getInstance();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Commands) {
            Commands commands = (Commands) msg;
            if(commands.equals(AUTH_OK)) {
                controller.setAuthenticated();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
