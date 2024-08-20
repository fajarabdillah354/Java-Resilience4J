import io.github.resilience4j.bulkhead.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Slf4j
public class BulkHeadTest {


    /** Bulkhead
     * Resilience4J memiliki module untuk menjaga jumlah eksekusi concurrent menggunakan Bulkhead
     * Terdapat dua implementasi Bulkhead di Resilience4J : Semaphore dan Fix Threadpool
     * Jika Bulkhead sudah penuh, secara otomatis Bulkhead akan mengembalikan error BulkheadFullException ketika kita meminta eksekusi
     */

    private final AtomicLong counter = new AtomicLong(0L);

    @SneakyThrows
    void sayHello(){
        log.info("Hello - "+counter.incrementAndGet());
        Thread.sleep(2_000L);//Sleep program
    }


    @Test
    void testSemaphoreBulkHead() throws InterruptedException {
        Bulkhead bulkhead = Bulkhead.ofDefaults("fjr");

        //jika menggunakan Semaphore yang sifatnya Fix

        //default pada saat concurrent lebih dari 25 Thread yang jalan lebih dari 25 maka ditolak
        for (int i = 0; i < 1000; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> sayHello());
            new Thread(runnable).start();
        }

        Thread.sleep(10_000);
    }


    @Test
    void testThreadPollBulkHead() {
        // melihat CPU core
        log.info(String.valueOf(Runtime.getRuntime().availableProcessors()));


        //jika menggunakan ThreadPoolBulkhead dia akan mengikuti jumlah procesor atau CPU yang ada di device kita
        ThreadPoolBulkhead bulkhead = ThreadPoolBulkhead.ofDefaults("fjr");


        for (int i = 0; i < 100; i++) {

            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, this::sayHello);
            supplier.get();

        }

    }


    /** Bulkhead Config
     * Sama seperti module lainnya, kita juga bisa melakukan pengaturan untuk Bulkhead
     * Namun pengaturannya disesuaikan dengan implementasi Bulkhead yang kita gunakan, baik itu Semaphore atau Fix Threadpool
     * @throws InterruptedException
     */


    @Test
    void testSemaphoreConfig() throws InterruptedException {

        /** Pengaturan Semaphore Bulkhead
         * maxConcurrentCalls, default(25) -> Maksimal eksekusi paralel yang diperbolehkan
         * maxWaitDuration. default(0) -> Maksimal durasi eksekusi menunggu bulkhead
         */

        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(5)//mambatasi proses concuren yang boleh dilakukan maksimal hanya 5,
                .maxWaitDuration(Duration.ofSeconds(5))//setelah 5 proses berhasil dijalankan program akan menunggu maksimal 5 detik untuk menjalankan proses berikutnya
                .build();

        Bulkhead bulkhead = Bulkhead.of("fjr",config);

        //memberi 10 proses
        for (int i = 0; i < 10; i++) {
            /*
            karena sleep yang ada di method saHello() 5 detik sementara pada BulkheadConfig waktu tunggu selama 5 detik maka sisa proses yang belum dijalankan akan dikembalikan sebagai error
             */
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, this::sayHello);
            new Thread(runnable).start();
        }

        Thread.sleep(10_000);

    }


    @Test
    void testThreadPollBulkHeadConfig() throws InterruptedException {
        /** Pengaturan Fix Threadpool Bulkhead
         * maxThreadPoolSize, default : Runtime.getRuntime().availableProcessor()] -> Maksimal thread yang terdapat di pool
         * coreThreadPoolSize, default : Runtime.getRuntime().availableProcessor()-1 -> Minimal thread awal yag terdapat di pool
         * queueCapacity, default : 100 -> Kapasitas antrian
         * keepAliveDuration, default : 20[ms] -> Lama thread hidup jika tidak bekerja
         */

        ThreadPoolBulkheadConfig bulkheadConfig = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(10)//nilai dari maxThreadPool harus lebih besar atau sama dengan coreThreadPoll
                .coreThreadPoolSize(5)//menentukan jumlah core inti sebanyak 5, dan akan menjalankan setiap 5 thread
//                .queueCapacity(1)//defaultnya 100
                .build();




        // melihat CPU core
        log.info(String.valueOf(Runtime.getRuntime().availableProcessors()));


        //jika menggunakan ThreadPoolBulkhead dia akan mengikuti jumlah procesor atau CPU yang ada di device kita
        ThreadPoolBulkhead bulkhead = ThreadPoolBulkhead.of("fjr",bulkheadConfig);


        for (int i = 0; i < 50; i++) {

            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, this::sayHello);
            supplier.get();

        }

        Thread.sleep(20_000L);//program akan menunggu selama 20 detik, setelah itu semua yang sedang berjalan dihentikan secara paksa

    }


    /** Bulkhead Registry
     * Sama dengan module lainnya, Bulkhead juga memiliki registry
     * Baik itu Semaphore Bulkhead, atau Fix Threadpool Bulkhead
     * @throws InterruptedException
     */


    @Test
    void testSemaphoreRegistry() throws InterruptedException {



        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(5)//mambatasi proses concuren yang boleh dilakukan maksimal hanya 5,
                .maxWaitDuration(Duration.ofSeconds(5))//setelah 5 proses berhasil dijalankan program akan menunggu maksimal 5 detik untuk menjalankan proses berikutnya
                .build();

        BulkheadRegistry registry = BulkheadRegistry.ofDefaults();
        registry.addConfiguration("config", config);

        Bulkhead bulkhead = registry.bulkhead("fjr",config);

        //memberi 10 proses
        for (int i = 0; i < 10; i++) {
            /*
            karena sleep yang ada di method saHello() 5 detik sementara pada BulkheadConfig waktu tunggu selama 5 detik maka sisa proses yang belum dijalankan akan dikembalikan sebagai error
             */
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, this::sayHello);
            new Thread(runnable).start();
        }

        Thread.sleep(10_000);

    }



    @Test
    void testThreadPollBulkHeadRegistry() throws InterruptedException {


        ThreadPoolBulkheadConfig bulkheadConfig = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(10)//nilai dari maxThreadPool harus lebih besar atau sama dengan coreThreadPoll
                .coreThreadPoolSize(5)//menentukan jumlah core inti sebanyak 5, dan akan menjalankan setiap 5 thread
//                .queueCapacity(1)//defaultnya 100
                .build();

        //ThreadPoolBulkheadRegistry
        ThreadPoolBulkheadRegistry registry = ThreadPoolBulkheadRegistry.ofDefaults();
        registry.addConfiguration("config",bulkheadConfig);


        // melihat CPU core
        log.info(String.valueOf(Runtime.getRuntime().availableProcessors()));


        //jika menggunakan ThreadPoolBulkhead dia akan mengikuti jumlah procesor atau CPU yang ada di device kita
        ThreadPoolBulkhead bulkhead = registry.bulkhead("fjr","config");


        for (int i = 0; i < 50; i++) {

            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, this::sayHello);
            supplier.get();

        }

        Thread.sleep(20_000L);//program akan menunggu selama 20 detik, setelah itu semua yang sedang berjalan dihentikan secara paksa

    }


    // note : ketika buat aplikasi selalu gunakan BulkheadRegistry
}
