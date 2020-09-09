import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) {
        testFiles();
    }

    private static void testFiles() {
        ExecutorService executorService = Executors.newFixedThreadPool(12);
        LinkedList<File> files = new LinkedList<>();

        File file = new File("E://test");
        if (file.isDirectory())
            files.add(file);
        while (!files.isEmpty()) {
            File dirs = files.removeFirst();
            File[] temp = dirs.listFiles();
            if (temp != null && temp.length > 0)
                for (File file1 : temp) {
                    if (file1.isDirectory())
                        files.add(file1);
                    else {
                        try {
                            long block = file1.length() / 8;
                            if (block > 1024 * 1024) {
                                FileChannel fileChannel = new RandomAccessFile(file1, "rw").getChannel();
                                for (int j = 0; j < 8; j++) {
                                    // For most operating systems, mapping a file into memory is more expensive than reading or writing a few tens of kilobytes of data via the usual {@link #read read} and {@link #write write} methods.  From the standpoint of performance it is generally only worth mapping relatively large files into memory.
                                    //对于大多数操作系统，将一个文件映射到内存比通过通常的{@link}read read}和{@link#write write}方法读取或写入几十KB的数据要昂贵得多。从内存映射到大文件的角度来看，它是值得的。

                                    //Read/write: Changes made to the resulting buffer will eventually be propagated to the file; they may or may not be made visible to other programs that have mapped the same file.
                                    //读/写：对结果缓冲区所做的更改最终将传播到该文件；它们可能对映射了同一文件的其他程序可见，也可能不可见。
                                    //Private: Changes made to the resulting buffer will not be propagated to the file and will not be visible to other programs that have mapped the same file; instead, they will cause private copies of the modified portions of the buffer to be created.
                                    //Private：对结果缓冲区所做的更改不会传播到该文件，并且对映射了同一文件的其他程序不可见；相反，它们将导致创建缓冲区修改部分的私有副本。
                                    MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, j * block, block);
                                    executorService.execute(new RWTest(file1.getName(), fileChannel, buffer, j * block, block));
                                    buffer.force();
                                }
                            }  // 文件太小
                            else {
                                executorService.execute(new RWrite(file1));
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }

        if (!executorService.isShutdown())
            executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /*
//    清理MappedByteBuffer   //Direct Buffer
    public static void clean(final Object buffer) throws Exception {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
                    getCleanerMethod.setAccessible(true);
                    sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
                    cleaner.clean();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }*/


}
