package com.chenyixin.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyServer {

    public static void main(String[] args)  {
        // 1、创建ServerBootstrap实例
        ServerBootstrap serviceBoostrap = new ServerBootstrap();
        // 2 绑定
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup woker = new NioEventLoopGroup();
        serviceBoostrap.group(boss,woker);
        //3、设置并绑定服务器端Channel  作为NIO服务端，
        // 需要创建ServerSocketChannel，对应的实现是NioServerSocketChannel
        serviceBoostrap.channel(NioServerSocketChannel.class);
        //4、链路建立的时候创建并初始化ChannelPipelineChannel
        // Pipeline的本质是一个负责处理网络事件的职责链，负责管理和执行ChannelHandler。
        // 网络事件以事件流的形式在ChannelPipeline中流转，
        // 由ChannelPipeline根据Channel|Handler的执行策略调度ChannelHandler的执行。
        // 典型的网络事件有：
        //链路注册
        //链路激活
        //链路断开
        //接收到请求信息
        //请求信息接收并处理完毕
        //发送应答消息
        //链路发生异常
        //用户自定义事件
        serviceBoostrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel serverChannel) throws Exception {
                serverChannel.pipeline().addLast(new ServerHandler());
            }
        });
//        serviceBoostrap.option(ChannelOption.SO_BACKLOG, 2048);         //连接缓冲池的大小
//        serviceBoostrap.childOption(ChannelOption.SO_KEEPALIVE, true);//维持链接的活跃，清除死链接
        serviceBoostrap.childOption(ChannelOption.TCP_NODELAY, true);//关闭延迟发送

        ChannelFuture channelFuture = null;
        try {
            channelFuture = serviceBoostrap.bind(9920).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            woker.shutdownGracefully();
        }


     }
}