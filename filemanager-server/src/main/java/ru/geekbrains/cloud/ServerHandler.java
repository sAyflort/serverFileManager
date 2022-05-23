package ru.geekbrains.cloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.commons.*;
import ru.geekbrains.cloud.dataBaseService.BaseAuthService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static ru.commons.Commands.*;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final String basePath  = "C:\\Users\\sAyflort\\IdeaProjects\\serverFileManager\\cloud\\";
    private String basePathClient;
    private String currentPath;
    private String[] basePaths;
    private BaseAuthService baseAuthService;
    private final static int SHORT_MB_20 = 20 * 1_000_000 - 500_000;

    private static final Logger LOGGER = LogManager.getLogger(ServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.baseAuthService = ServerApp.getBaseAuthService();
        LOGGER.info("Пользователь подсоединился");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof SendFileRequest) {
            SendFileRequest sendFileRequest = (SendFileRequest) msg;
            if(sendFileRequest.getType() == REG) {
                Commands sendCmd = baseAuthService.reg(sendFileRequest.getLogin(), sendFileRequest.getPass()) == true ? REG_OK : REG_BAD;
                ctx.channel().writeAndFlush(sendCmd);
            }
            if(baseAuthService.isAuth(sendFileRequest.getLogin(), sendFileRequest.getPass())) {
                LOGGER.info("Получен запрос: "+sendFileRequest.getType());
                switch (sendFileRequest.getType()) {
                    case AUTH -> {
                        ctx.channel().writeAndFlush(AUTH_OK);
                        basePathClient = basePath + sendFileRequest.getLogin();
                        currentPath = basePathClient;
                        basePaths = basePathClient.split("\\\\");
                        ctx.channel().writeAndFlush(new FilesListRequest(basePathClient));
                    }
                    case CREATE_DIRECTORY -> {
                        String[] path = sendFileRequest.getDirectPath().split("\\\\");
                        if(!checkRootPath(sendFileRequest.getDirectPath()) || path[path.length-1].indexOf('.') != -1) {
                            return;
                        }
                        Files.createDirectories(Path.of(sendFileRequest.getDirectPath()));
                    }
                    case DELETE_FILE -> {
                        if (!checkRootPath(sendFileRequest.getDirectPath()))
                            return;
                        Files.deleteIfExists(Paths.get(sendFileRequest.getDirectPath()));
                    }
                    case SEND_FILE -> {
                        String path = currentPath;
                        Files.createDirectories(Path.of(path));
                        path += "\\"+ sendFileRequest.getFileInfo().getFileName();

                        Files.deleteIfExists(Path.of(path));
                        Files.createFile(Path.of(path));
                        Files.write(Path.of(path),
                                sendFileRequest.getFileInfo().getFile(), StandardOpenOption.APPEND);
                    }
                    case PART_FILE -> {
                        String path = currentPath + "\\" + sendFileRequest.getFileInfo().getFileName();
                        Files.write(Path.of(path),
                                sendFileRequest.getFileInfo().getFile(), StandardOpenOption.APPEND);
                        if(!(Files.size(Path.of(path)) == sendFileRequest.getFileInfo().getSize())) {
                           return;
                        }
                    }
                    case GET_FILE -> {
                        if (!checkRootPath(sendFileRequest.getFileInfo().getFilePath()))
                            return;
                        sendFile(new FileInfo(Paths.get(sendFileRequest.getFileInfo().getFilePath())), SEND_FILE, ctx);
                    }
                    case GET_FILE_LIST -> {
                        if (!checkRootPath(sendFileRequest.getDirectPath()))
                            return;
                        currentPath = sendFileRequest.getDirectPath();
                    }
                    case SEARCH_FILE -> {
                        ctx.channel().writeAndFlush(new FilesListRequest(Files.walk(Paths.get(currentPath))
                                .filter(item -> item.getFileName().toString().equals(sendFileRequest.getDirectPath()))
                                .map(item -> new File(item.toString()))
                                .toList()));
                        return;
                    }
                }
                ctx.channel().writeAndFlush(new FilesListRequest(currentPath));
            } else {
                ctx.channel().writeAndFlush(AUTH_BAD);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        LOGGER.info("Завершений программы");
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
        LOGGER.info(command);
        if (command == SEND_FILE) {
            AtomicBoolean isBeginFile = new AtomicBoolean(true);
            Consumer<byte[]> filePartResponse = bytes -> {
                SendFileRequest sendFileRequest;
                if(isBeginFile.get()) {
                    sendFileRequest = new SendFileRequest(SEND_FILE, null, null, null, new FileInfo(msg, bytes));
                    isBeginFile.set(false);
                } else {
                    sendFileRequest = new SendFileRequest(PART_FILE, null, null, null, new FileInfo(msg, bytes));
                }
                ctx.writeAndFlush(sendFileRequest);
            };
            FileSplit fileSplit = new FileSplit();
            fileSplit.split(Paths.get(msg.getFilePath()), filePartResponse);
        }
    }

    public boolean checkRootPath(String path) {
        String[] checkPath = path.split("\\\\");
        try {
            for (int i = 0; i < basePaths.length; i++) {
                if (!checkPath[i].equals(basePaths[i])) {
                    return false;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }
}
