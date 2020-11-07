package com.chenyixin.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

    public static void main(String[] args) {
        Bootstrap  bootstrap = new Bootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        bootstrap.group(boss);
        bootstrap.channel(NioSocketChannel.class);
        // client 没有childHandler
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel serverChannel) throws Exception {
                serverChannel.pipeline().addLast(new ClientHandler());
            }
        });

        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect("192.168.199.105",9920).sync();
            for(int i =0;i<1000;i++) {
                channelFuture.channel().writeAndFlush(Unpooled.buffer().writeBytes("12122".getBytes()));

            }

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
        }

    }


}
