package ru.geekbrains.cloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.commons.Commands;
import ru.commons.FileInfo;
import ru.commons.FilesListRequest;
import ru.commons.SendFileRequest;
import ru.geekbrains.cloud.dataBaseService.BaseAuthService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static ru.commons.Commands.*;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final String basePath  = "C:\\Users\\sAyflort\\IdeaProjects\\serverFileManager\\cloud\\";
    private BaseAuthService baseAuthService;
    private final static int SHORT_MB_20 = 20 * 1_000_000 - 500_000;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.baseAuthService = ServerApp.getBaseAuthService();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof SendFileRequest) {
            SendFileRequest sendFileRequest = (SendFileRequest) msg;

            System.out.println(sendFileRequest.getType());

            if(baseAuthService.isAuth(sendFileRequest.getLogin(), sendFileRequest.getPass())) {
                if(sendFileRequest.getType() == AUTH) {
                    ctx.channel().writeAndFlush(AUTH_OK);
                    ctx.channel().writeAndFlush(new FilesListRequest(basePath+sendFileRequest.getLogin()));
                    return;
                }
                if(sendFileRequest.getType() == SEND_FILE) {
                    String path = basePath+ sendFileRequest.getLogin();
                    Files.createDirectories(Path.of(path));
                    path += "\\"+ sendFileRequest.getFileInfo().getFileName();

                    Files.deleteIfExists(Path.of(path));
                    Files.createFile(Path.of(path));
                    Files.write(Path.of(path),
                            sendFileRequest.getFileInfo().getFile(), StandardOpenOption.APPEND);
                }
                if(sendFileRequest.getType() == PART_FILE) {
                    String path = basePath+ sendFileRequest.getLogin()+"\\"+ sendFileRequest.getFileInfo().getFileName();
                    Files.write(Path.of(path),
                            sendFileRequest.getFileInfo().getFile(), StandardOpenOption.APPEND);
                }
                if(sendFileRequest.getType() == GET_FILE) {
                    System.out.println("Запрос получен");
                    String[] cutPath = sendFileRequest.getFileInfo().getFilePath().split("\\\\");
                    if(cutPath[6].equals(sendFileRequest.getLogin()) &&
                            (sendFileRequest.getFileInfo().getFilePath().split("\\\\"+sendFileRequest.getLogin())[0]+"\\").equals(basePath)) {
                        /*ctx.channel().writeAndFlush(new SendFileRequest(SEND_FILE, null, null, null,
                                new FileInfo(Paths.get(sendFileRequest.getFileInfo().getFilePath()))));*/
                        sendFile(new FileInfo(Paths.get(sendFileRequest.getFileInfo().getFilePath())), SEND_FILE, ctx);
                    } else {
                        return;
                    }
                }
                ctx.channel().writeAndFlush(new FilesListRequest(basePath+sendFileRequest.getLogin()));
            } else {
                return;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
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

    public void sendFile(FileInfo msg, Commands command, ChannelHandlerContext ctx) {
        if (command == SEND_FILE) {
            List<FileInfo> listFiles = cutFile(msg);
            ctx.channel().writeAndFlush(new SendFileRequest(SEND_FILE, null, null, null, listFiles.get(0)));
            if(listFiles.size() > 1) {
                for (int i = 1; i < listFiles.size(); i++) {
                    ctx.channel().writeAndFlush(new SendFileRequest(PART_FILE, null, null, null, listFiles.get(i)));
                }
            }
        }
    }
}
