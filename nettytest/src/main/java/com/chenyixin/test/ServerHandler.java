package com.chenyixin.test;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {


   @Override
   public void channelActive(ChannelHandlerContext ctx) throws Exception
   {
       System.out.println("来自客户端的连接连上了...");
       ctx.writeAndFlush(Unpooled.buffer().writeBytes("来自服务端的连接连上了...".getBytes()));
   }
   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       System.out.println("客户端发送" +msg.toString());
       ctx.writeAndFlush(Unpooled.buffer().writeBytes("来自服务端的连接连上了...".getBytes()));
   }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //将未决消息冲刷到远程节点，并关闭该Channel
        ctx.flush();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();  //打印异常栈追踪
        ctx.close(); //关闭该channel
    }
    }
