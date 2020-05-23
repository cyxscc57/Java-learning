package com.atguigu.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

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
public class TestNonBlockingNIO {

    @Test
    public void client() throws IOException {
        //1、获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));

        //2、切换非阻塞模式
        socketChannel.configureBlocking(false);

        //3、分配缓冲区
        ByteBuffer buf=ByteBuffer.allocate(1024);

        //4、发送数据给服务端
        Scanner scan=new Scanner(System.in);
        while(scan.hasNext()){
            String str=scan.next();
            buf.put((new Date().toString()+"\n"+str).getBytes());
            buf.flip();
            socketChannel.write(buf);
            buf.clear();
        }


        //5、关闭通道
        socketChannel.close();
    }

    @Test
    public void server() throws IOException {
        //1、获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //2、切换非阻塞模式
        serverSocketChannel.configureBlocking(false);

        //3、绑定端口
        serverSocketChannel.bind(new InetSocketAddress(9898));

        //4、获取选择器
        Selector selector = Selector.open();

        //5、将通道注册到选择器上，并指定“监听接受事件”
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //6、轮询式的获取注册到选择器上的已经准备就绪的事件
        while(selector.select()>0){
            //7、获取当前选择器上所有注册的选择键（已就绪的监听事件）
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();

            while(it.hasNext()){
                //8、获取准备就绪的事件
                SelectionKey selectionKey = it.next();
                //9、判断具体是什么事件准备就绪
                if(selectionKey.isAcceptable()){
                    //10、若接受就绪，获取客户端的连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //11、切换为非阻塞模式
                    socketChannel.configureBlocking(false);
                    //12、将该通道注册到选择器上
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else if(selectionKey.isReadable()){
                    //13、获取当前选择器上读状态就绪的通道
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    //14、读取数据
                    ByteBuffer buf=ByteBuffer.allocate(1024);
                    int len=0;
                    while((len=channel.read(buf))>0){
                        buf.flip();
                        System.out.println(new String(buf.array(),0,len));
                        buf.clear();
                    }
                }
                //15、取消选择键 SelectionKey
                it.remove();
            }
        }
    }
}
