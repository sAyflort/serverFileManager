package ru.geekbrains.serverFileManager.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.commons.Commands;
import ru.commons.FilesListRequest;
import ru.commons.SendFileRequest;
import ru.geekbrains.serverFileManager.Controller;
import ru.geekbrains.serverFileManager.ServerPanelCtrl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Controller controller;
    private ServerPanelCtrl panelController;
    private String currentPath;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.controller = Controller.getInstance();
        this.panelController = this.controller.getRightPController();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        currentPath = controller.getCurrentPath();
        if (msg instanceof Commands) {
            Commands command = (Commands) msg;
            switch (command) {
                case AUTH_OK -> {
                    controller.setAuthenticated();
                }
                case AUTH_BAD -> {
                    controller.appendAuthText("Неверный логин/пароль\n");
                }
                case REG_OK -> {
                    controller.appendRegText("Регистрация прошла успешно\n");
                }
                case REG_BAD -> {
                    controller.appendRegText("Такой пользователь уже существует\n");
                }
            }
        }
        if (msg instanceof FilesListRequest) {
            FilesListRequest filesListRequest = (FilesListRequest) msg;
            panelController.updateTable(filesListRequest.getFileList(), filesListRequest.getPath());
        }
        if (msg instanceof SendFileRequest) {
            SendFileRequest request = (SendFileRequest) msg;
            switch (request.getType()) {
                case SEND_FILE -> {
                    currentPath += "\\"+ request.getFileInfo().getFileName();
                    Files.deleteIfExists(Path.of(currentPath));
                    Files.createFile(Path.of(currentPath));
                    Files.write(Path.of(currentPath),
                            request.getFileInfo().getFile(), StandardOpenOption.APPEND);
                    controller.updateLeftTable();
                }
                case PART_FILE -> {
                    currentPath += "\\"+ request.getFileInfo().getFileName();
                    Files.write(Path.of(currentPath),
                            request.getFileInfo().getFile(), StandardOpenOption.APPEND);
                    controller.updateLeftTable();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
