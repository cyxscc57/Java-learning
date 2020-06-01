package com.atguigu.nio;

import jdk.management.resource.internal.inst.SocketOutputStreamRMHooks;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestBlockingNIO2 {
    @Test
    public void client() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
        FileChannel fileChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);

        ByteBuffer buf=ByteBuffer.allocate(1024);

        while(fileChannel.read(buf)!=-1){
            buf.flip();
            socketChannel.write(buf);
            buf.clear();
        }
        System.out.println(1111);
        //调用此方法后客户端的输出流才会关闭，服务端才会读到末尾-1，解除都阻塞的状态
        socketChannel.shutdownOutput();
        int len=0;
        //接受服务端提醒
        while((len=socketChannel.read(buf))!=-1){
            buf.flip();
            System.out.println(new String(buf.array(),0,len));
            buf.clear();
        }
        fileChannel.close();
        socketChannel.close();

    }

    @Test
    public void server() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(9898));

        SocketChannel socketChannel = serverSocketChannel.accept();

        FileChannel fileChannel=FileChannel.open(Paths.get("3.jpg"),StandardOpenOption.WRITE,StandardOpenOption.CREATE);

        ByteBuffer buf=ByteBuffer.allocate(1024);

        while(socketChannel.read(buf)!=-1){
            buf.flip();
            fileChannel.write(buf);
            buf.clear();
        }
        System.out.println(buf.hasRemaining());
        //发送通知给客户端
        buf.put("服务端接收数据完成".getBytes());
        System.out.println(buf);
        buf.flip();
        System.out.println(buf);
        socketChannel.write(buf);

        fileChannel.close();
        socketChannel.close();
        serverSocketChannel.close();
    }
}