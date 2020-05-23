package com.atguigu.nio;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

/*
 * 一、通道（Channel）：用于源节点与目标节点的连接。在 Java NIO 中负责缓冲区中数据的传输。Channel 本身不存储数据，因此需要配合缓冲区进行传输。
 *
 * 二、通道的主要实现类
 * 	java.nio.channels.Channel 接口：
 * 		|--FileChannel
 * 		|--SocketChannel
 * 		|--ServerSocketChannel
 * 		|--DatagramChannel
 *
 * 三、获取通道
 * 1. Java 针对支持通道的类提供了 getChannel() 方法
 * 		本地 IO：
 * 		FileInputStream/FileOutputStream
 * 		RandomAccessFile
 *
 * 		网络IO：
 * 		Socket
 * 		ServerSocket
 * 		DatagramSocket
 *
 * 2. 在 JDK 1.7 中的 NIO.2 针对各个通道提供了静态方法 open()
 * 3. 在 JDK 1.7 中的 NIO.2 的 Files 工具类的 newByteChannel()
 *
 * 四、通道之间的数据传输
 * transferFrom()
 * transferTo()
 *
 * 五、分散(Scatter)与聚集(Gather)
 * 分散读取（Scattering Reads）：将通道中的数据分散到多个缓冲区中
 * 聚集写入（Gathering Writes）：将多个缓冲区中的数据聚集到通道中
 *
 * 六、字符集：Charset
 * 编码：字符串 -> 字节数组
 * 解码：字节数组  -> 字符串
 *
 */
public class TestChannel {
    //6 编码和解码
    @Test
    public void test6() throws IOException {
        Charset charset = Charset.forName("UTF-8");
        //获得编码器
        CharsetEncoder charsetEncoder = charset.newEncoder();
        //获得解码器
        CharsetDecoder charsetDecoder = charset.newDecoder();

        CharBuffer cBuf = CharBuffer.allocate(1024);
        cBuf.put("NIO有点难");
        //移动指针到初始位置
        cBuf.flip();
        ByteBuffer bBuf = charsetEncoder.encode(cBuf);
        for (int i = 0; i <12 ; i++) {
            System.out.println(bBuf.get());
        }
        bBuf.flip();
        CharBuffer charBuffer = charsetDecoder.decode(bBuf);
        System.out.println(charBuffer.toString());
    }
    //5 字符集
    @Test
    public void test5(){
        Map<String,Charset> charsetMap =Charset.availableCharsets();
        Set<Map.Entry<String, Charset>> entrySet = charsetMap.entrySet();
        for (Map.Entry<String, Charset> stringCharsetEntry : entrySet) {
            System.out.println(stringCharsetEntry.getKey()+":"+stringCharsetEntry.getValue());
        }
    }
    //4、分散和读取
    @Test
    public void test4() throws IOException {
        RandomAccessFile raf1=new RandomAccessFile("1.txt","rw");
        //1、获取通道
        FileChannel fileChannel1 = raf1.getChannel();
        //2、分配指定大小缓冲区
        ByteBuffer buf1=ByteBuffer.allocate(13);
        ByteBuffer buf2=ByteBuffer.allocate(1024);
        //3、分散读取
        ByteBuffer[] bufs={buf1,buf2};
        fileChannel1.read(bufs);
        for (ByteBuffer buf : bufs) {
            buf.flip();
        }
        System.out.println(new String(bufs[0].array(),0,bufs[0].limit()));
        System.out.println(new String(bufs[1].array(),0,bufs[1].limit()));
        //聚集
        RandomAccessFile raf2=new RandomAccessFile("2.txt","rw");
        FileChannel fileChannel2 = raf2.getChannel();
        fileChannel2.write(bufs);
    }
    //3、通道之间的数据传输
    @Test
    public void test3()throws IOException{
        FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("4.jpg"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        inChannel.transferTo(0,inChannel.size(),outChannel);

        inChannel.close();
        outChannel.close();

    }
    //2、使用直接缓冲区完成复制
    @Test
    public void test2() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("3.jpg"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        //内存映射文件
        MappedByteBuffer inBuf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        MappedByteBuffer outBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());
        //直接对缓冲区进行读写操作
        byte[] dst=new byte[inBuf.limit()];
        inBuf.get(dst);
        outBuf.put(dst);

        inChannel.close();
        outChannel.close();

    }
    //1、利用通道完成文件的复制（非直接缓冲区）
    @Test
    public void test1() {
        FileInputStream fis = null;
        FileOutputStream fos= null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            fis = new FileInputStream("1.jpg");
            fos = new FileOutputStream("2.jpg");

            //获取通道
            inChannel = fis.getChannel();
            outChannel = fos.getChannel();

            //创建缓冲区
            ByteBuffer buf=ByteBuffer.allocate(1024);

            while(inChannel.read(buf)!=-1){
                //缓冲区切换成写模式
                buf.flip();
                outChannel.write(buf);
                //每次写完要清空
                buf.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(inChannel!=null){
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(outChannel!=null){
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }





    }
}
