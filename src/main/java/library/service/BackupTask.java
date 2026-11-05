package library.service;
import library.util.Log;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class BackupTask implements Runnable {
    private final Library library;
    private final Path backupFolder;
 private final long intervalMillis;
 private final AtomicInteger backupCount = new AtomicInteger();
    private volatile boolean running = true;
    private Thread thread;
/////
public BackupTask(Library library, Path backupFolder, long intervalMillis) {
        this.library= library;
            this.backupFolder= backupFolder;
          this.intervalMillis=intervalMillis;
    }
//////
    public void start() {
    thread =new Thread(this, "backup-thread");
     thread.setDaemon(true);
        thread.start();
    }
/////
 public void stop() {
          running =false;
        if (thread != null) {
            thread.interrupt();  }
    }
/////
    @Override
public void run() {
        while (running) {
            try 
            {
            Thread.sleep(intervalMillis);
            } catch (InterruptedException e) 
            {
                break;
            }
            if (running) {
             backupNow();
            }
        }
    }
////

    public void backupNow() {
        try {
                library.saveTo(backupFolder);
            int number =backupCount.incrementAndGet();
                Log.debug("Backup #" + number + " saved to " + backupFolder);
        } catch (IOException e) {
        Log.info("Backup failed: " + e.getMessage());
        }
    }
////
    public int getBackupCount() {
      return backupCount.get();
    }
}
