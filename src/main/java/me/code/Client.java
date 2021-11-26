package me.code;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.code.util.BufferUtil;

public class Client {

    private final String address;
    private final int port;
    private final EventLoopGroup eventLoopGroup;

    public Client(String address, int port) {
        this.port = port;
        this.address = address;
        this.eventLoopGroup = new NioEventLoopGroup();
    }

    public void start() {
        Bootstrap bootstrap = new Bootstrap();

        try {
            Channel channel = bootstrap.group(this.eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ClientHandler());
                        }
                    })
                    .connect(address, port).sync().channel();


            sendIntentionPacket(channel);
            sendRequestPacket(channel);
            sendPingRequestPacket(channel);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendIntentionPacket(Channel channel) {
        ByteBuf buf = Unpooled.buffer();

        int protocolVersion = 756;
        String serverAddress = "localhost";
        int port = 25565;
        int nextState = 1;

        int packetId = 0;
        int length = BufferUtil.getVarIntSize(protocolVersion) +
                BufferUtil.getVarIntSize(serverAddress.length()) +
                serverAddress.length() +
                2 +
                BufferUtil.getVarIntSize(nextState) +
                BufferUtil.getVarIntSize(packetId);


        BufferUtil.writeVarInt(length, buf);
        BufferUtil.writeVarInt(packetId, buf);
        BufferUtil.writeVarInt(protocolVersion, buf);
        BufferUtil.writeVarInt(serverAddress.length(), buf);
        buf.writeBytes(serverAddress.getBytes());
        buf.writeShort(port);
        BufferUtil.writeVarInt(nextState, buf);

        channel.writeAndFlush(buf);
    }

    private void sendRequestPacket(Channel channel) {
        ByteBuf buf = Unpooled.buffer();

        int packetId = 0;
        int length = BufferUtil.getVarIntSize(packetId);


        BufferUtil.writeVarInt(length, buf);
        BufferUtil.writeVarInt(packetId, buf);

        channel.writeAndFlush(buf);
    }

    private void sendPingRequestPacket(Channel channel) {
        ByteBuf buf = Unpooled.buffer();

        int packetId = 0;
        int length = 8 + BufferUtil.getVarIntSize(packetId);


        BufferUtil.writeVarInt(length, buf);
        BufferUtil.writeVarInt(packetId, buf);
        buf.writeLong(System.currentTimeMillis());

        channel.writeAndFlush(buf);
    }
}
