import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

//修改1KB数据
public class RWrite implements Runnable {
    private static byte[] chaos = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
    private String filename;
    private int size;
    private File file;


    public RWrite(File file1) {
        filename = file1.getName();
        size = file1.length() > 1024 * 8 ? 1024 * 8 : size;
        file = file1;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        byte[] byteBuffer = new byte[size];
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            randomAccessFile.read(byteBuffer);
            convert(byteBuffer);
            randomAccessFile.seek(0);
            randomAccessFile.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }


        long stop = System.currentTimeMillis();
        System.out.println(String.format("%s 耗费 %d毫秒", this, (stop - start)));
    }

    private void convert(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (chaos[i % 16] ^ bytes[i]);
        }
    }

    @Override
    public String toString() {
        return String.format("Thread %s-%d   covert:%s ", Thread.currentThread().getName(), Thread.currentThread().getId(), filename);
    }
}
/**
 * 读写线程操作文件
 */
/*
public class RWrite extends Thread {
    private static byte[] chaos = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
    private String file;
    private long position;
    private long block;

    RWrite(String file, long position, long block) {
        this.file = file;
        this.position = position;
        this.block = block;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            try (FileChannel fileChannel = randomAccessFile.getChannel()) {

                FileLock fileLock = null;
                while (true) {
                    try {
//                fileLock = fileChannel.tryLock(position, block, true);
                        fileLock = fileChannel.tryLock(position, block, false);
                        break;
                    } catch (IOException e) {
                        System.out.println("there is another thread here.");
                        try {
                            sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }

                // do something ...
//                System.out.println(this + "converting !!!!");
                byte[] bytes = new byte[(int) (block & 0x7fffffff)];
                try {
                    randomAccessFile.seek(position);
                    randomAccessFile.read(bytes, 0, bytes.length);
                    convert(bytes);

                    randomAccessFile.seek(position);
                    randomAccessFile.write(bytes);
                } catch (IOException e) {
                    System.err.println(this);
                    e.printStackTrace();
                }
                try {
                    if (fileLock != null)
                        fileLock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                long stop = System.currentTimeMillis();
                System.out.println(String.format("%s 耗费 %d毫秒", this, (stop - start)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convert(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (chaos[i % 16] ^ bytes[i]);
//            bytes[i] = (byte) (0x03 ^ bytes[i]);
        }
    }

    @Override
    public String toString() {
        return String.format("Thread %s-%d position:%d  block: %d    covert:%s ", Thread.currentThread().getName(), Thread.currentThread().getId(), position, block, file);
    }
}
*/