package ru.geekbrains.serverFileManager.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ru.geekbrains.serverFileManager.FileInfo;

import java.nio.charset.StandardCharsets;

public class FileInfoToBytesEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof FileInfo) {
            FileInfo fi = (FileInfo) msg;
            String newMsg = fi.getFileName()+"#"+fi.getSize()+"#$";
            out.writeBytes(concat(newMsg.getBytes(StandardCharsets.UTF_8), fi.getFile()));
        }
    }

    private byte[] concat(byte[] bytes1, byte[] bytes2) {
        byte[] bytes = new byte[bytes1.length+bytes2.length];
        for (int i = 0; i < bytes1.length; i++) {
            bytes[i] = bytes1[i];
        }
        for (int i = bytes1.length; i < bytes.length; i++) {
            bytes[i] = bytes2[i-(bytes1.length)];
        }
        return bytes;
    }
}
