package ru.geekbrains.cloud;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class BytesToFileDecoder extends ByteToMessageDecoder {
    private boolean flag;
    private StringBuilder stringBuilder = new StringBuilder();
    private static final char END = '$';
    private Path path;
    private int count;
    private int size;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!this.flag) {
            while (in.isReadable()) {
                this.stringBuilder.append((char) in.readByte());
                this.flag = stringBuilder.charAt(stringBuilder.length()-1) == END;
                if(this.flag) break;
            }
            String str = this.stringBuilder.toString();
            this.path = Paths.get("C:\\Users\\sAyflort\\Documents\\"+str.split("#", 2)[0]);
            this.size = Integer.parseInt(str.split("#")[1]);
            Files.createFile(path);
        }
        StringBuilder strb = new StringBuilder();
        while (in.isReadable()) {
            count++;
            if (count <= size)
                strb.append((char) in.readByte());
        }

        Files.write(this.path, strb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        //String str = stringBuffer.toString();
        //Files.createFile(Path.of("C:\\Users\\sAyflort\\Documents\\"+str.split("#", 2)[0]));
        //System.out.println(str.split("#", 2)[0]);
    }
}
