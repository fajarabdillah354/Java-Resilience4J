import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@Slf4j
public class RetryRegisterTest {


    /** Retry Registry
     * Saat kita belajar Java Database, kita mengenal yang namanya Database Pooling, yaitu tempat untuk menyimpan semua koneksi database
     * Resilience4J juga mengenal konsep ini, namun namanya adalah Registry
     * Registry adalah tempat untuk menyimpan object-object dari Resilience4J
     * Dengan menggunakan Registry, secara otomatis kita bisa menggunakan ulang object yang sudah kita buat, tanpa harus membuat ulang object baru
     * Penggunaan Registry adalah salah satu best practice yang direkomendasikan ketika menggunakan Resilience4J
     *
     */


    void sayHello(){
        log.info("Call sayHello method");
        throw new IllegalArgumentException("ups error");
    }


    @Test
    void testRetryRegister() {

        RetryRegistry retryRegistry = RetryRegistry.ofDefaults();


        Retry retry1 = retryRegistry.retry("fjr");
        Retry retry2 = retryRegistry.retry("fjr");

        Assertions.assertSame(retry1,retry2);

    }

    /** Config di Registry
     * Salah satu hal yang menarik di Retry Registry, kita bisa menambahkan default config atau menambahkan config yang sama dengan nama Retry nya
     * Jika saat membuat Retry kita tidak menyebutkan nama config nya, secara otomatis akan menggunakan default config
     */

    @Test
    void testRetryRegisterConfig() {

        //kita buat RetryConfig terlebih dahulu dengan menggunakan method costum lalu tambahkan config yang kita inginkna
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofSeconds(2))
                .build();

        //pada RetryRegister kita harus tambahkan addConfiguration() dari rentryConfig yang telah kita buat
        RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
        retryRegistry.addConfiguration("retryConfig", retryConfig);

        //kita tinggal masukan config yang sudah kita masukan ke dalam RetryRegister
        Retry retry1 = retryRegistry.retry("fjr", "retryConfig");
        Retry retry2 = retryRegistry.retry("fjr","retryConfig");


        Runnable runnable = Retry.decorateRunnable(retry1, this::sayHello);
        runnable.run();




    }







}
