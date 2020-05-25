package com.atguigu.nio;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class TestPipe {
    @Test
    public void test1() throws IOException {
        //1、获取管道
        Pipe pipe=Pipe.open();
        //2、将缓冲区数据写入管道
        ByteBuffer buf=ByteBuffer.allocate(1024);
        Pipe.SinkChannel sink = pipe.sink();
        buf.put("通过管道发送数据".getBytes());
        buf.flip();
        sink.write(buf);

        //3、读取缓冲区的数据
        Pipe.SourceChannel sourceChannel = pipe.source();
        buf.flip();
        int len = sourceChannel.read(buf);
        System.out.println(new String(buf.array(),0,len));

        sink.close();
        sourceChannel.close();
    }
}
