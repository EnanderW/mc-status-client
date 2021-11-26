package me.code;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.code.util.BufferUtil;

public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        while (buf.readableBytes() > 0) {
            int length = BufferUtil.readVarInt(buf);
            int packetId = BufferUtil.readVarInt(buf);

            if (packetId == 0) {

                String json = BufferUtil.readVarString(buf);

                System.out.println(json);

            } else if (packetId == 1) {
                long time = buf.readLong();

                System.out.println("MS: " + (System.currentTimeMillis() - time));
            }

        }

    }

}
