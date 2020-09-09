import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class RWTest implements Runnable {

    private static final int SIZE = 1024000;
    private static byte[] chaos = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
    private FileChannel fileChannel;
    private String name;
    private MappedByteBuffer buffer;
    private long position;
    private long block;

    RWTest(String name, FileChannel fileChannel, MappedByteBuffer buffer, long position, long block) {
        this.name = name;
        this.fileChannel = fileChannel;
        this.buffer = buffer;
        this.position = position;
        this.block = block;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        FileLock fileLock = null;
        while (true) {
            try {
                fileLock = fileChannel.tryLock(position, block, false);
                break;
            } catch (IOException e) {
                System.out.println("there is another thread here.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

//        System.out.println(this + "converting !!!!");
        // do something ...
        int index = 0;
        while (buffer.hasRemaining() && index < SIZE) {
            buffer.put((byte) (buffer.get(index) ^ chaos[index % 16]));
            index++;
        }
        try {
            if (fileLock != null)
                fileLock.release();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long stop = System.currentTimeMillis();
        System.out.println(String.format("%s 耗费 %d毫秒", this, stop - start));
    }


    @Override
    public String toString() {
        return String.format("Thread %s-%d \tposition:%d  block: %d    covert:%s ", Thread.currentThread().getName(), Thread.currentThread().getId(), position, block, name);
    }

}
