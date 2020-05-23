package com.atguigu.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/*
 * 一、使用 NIO 完成网络通信的三个核心：
 *
 * 1. 通道（Channel）：负责连接
 *
 * 	   java.nio.channels.Channel 接口：
 * 			|--SelectableChannel
 * 				|--SocketChannel
 * 				|--ServerSocketChannel
 * 				|--DatagramChannel
 *
 * 				|--Pipe.SinkChannel
 * 				|--Pipe.SourceChannel
 *
 * 2. 缓冲区（Buffer）：负责数据的存取
 *
 * 3. 选择器（Selector）：是 SelectableChannel 的多路复用器。用于监控 SelectableChannel 的 IO 状况
 *
 */
public class TestBlockingNIO {
    //客户端
    @Test
    public void client() throws IOException {
        //1、获得通道
        SocketChannel ssChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));

        FileChannel inChannel=FileChannel.open(Paths.get("1.jpg"),StandardOpenOption.READ);
        //2、分配指定缓冲区大小
        ByteBuffer buf=ByteBuffer.allocate(1024);
        //3、读取本地文件发送给服务器
        while(inChannel.read(buf)!=-1){
            buf.flip();
            ssChannel.write(buf);
            buf.clear();
        }
        //4、关闭通道
        ssChannel.close();
        inChannel.close();
    }
    @Test
    public void server() throws IOException {
        //1、获取通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        FileChannel outChannel=FileChannel.open(Paths.get("2.jpg"),StandardOpenOption.WRITE,StandardOpenOption.CREATE);
        //2、绑定连接
        serverChannel.bind(new InetSocketAddress(9898));

        //3、获取客户端连接的通道
        SocketChannel socketChannel = serverChannel.accept();
        //4、创建缓冲区
        ByteBuffer buf=ByteBuffer.allocate(1024);
        //5、读取并写入本地
        while (socketChannel.read(buf)!=-1){
            buf.flip();
            outChannel.write(buf);
            buf.clear();
        }
        //6、关闭通道
        serverChannel.close();
        outChannel.close();
        socketChannel.close();
    }
}
