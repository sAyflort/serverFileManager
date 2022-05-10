package ru.geekbrains.serverFileManager.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.commons.Commands;
import ru.commons.FilesListRequest;
import ru.commons.SendFileRequest;
import ru.geekbrains.serverFileManager.Controller;
import ru.geekbrains.serverFileManager.PanelController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static ru.commons.Commands.*;

//Добавить логи
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private Controller controller;
    private PanelController panelController;
    private String currentPath;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.controller = Controller.getInstance();
        this.panelController = this.controller.getRightPController();
    }

    //Убрать ад из if, заменить на switch-case или работать через класс новый класс FileServiceManager
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        currentPath = controller.getCurrentPath();
        if (msg instanceof Commands) {
            Commands commands = (Commands) msg;
            if(commands.equals(AUTH_OK)) {
                controller.setAuthenticated();
            }
        }
        if (msg instanceof FilesListRequest) {
            FilesListRequest filesListRequest = (FilesListRequest) msg;
            panelController.updateTable(filesListRequest.getFileList(), filesListRequest.getPath());
        }
        if (msg instanceof SendFileRequest) {
            SendFileRequest request = (SendFileRequest) msg;
            if (request.getType() == SEND_FILE) {
                currentPath += "\\"+ request.getFileInfo().getFileName();
                Files.deleteIfExists(Path.of(currentPath));
                Files.createFile(Path.of(currentPath));
                Files.write(Path.of(currentPath),
                        request.getFileInfo().getFile(), StandardOpenOption.APPEND);
            }
            if (request.getType() == PART_FILE) {
                currentPath += "\\"+ request.getFileInfo().getFileName();
                Files.write(Path.of(currentPath),
                        request.getFileInfo().getFile(), StandardOpenOption.APPEND);
            }
        }
        controller.updateLeftTable();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
