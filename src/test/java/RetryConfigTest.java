import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
public class RetryConfigTest {


    /** Retry Config
     * Saat kita membuat Retry menggunakan Retry.ofDefaults(), secara otomatis kita akan menggunakan pengaturan default
     * Kadang pada kasus tertentu, kita ingin menentukan pengaturan untuk Retry secara manual, misal menentukan jumlah retry nya misalnya
     * Kita bisa membuat object RetryConfig sebelum membuat object Retry

     Pengaturan Retry
     maxAttempts -> default(3) , keterangan berapa banyak minimal Retry dilakukan
     waitDuration -> default(500 ms) , waktu menunggu sebelum melakukan retry
     ignoreExceptions -> empty , jenis error yang tidak akan di retry
     retryExceptions -> empty , jenis error yang akand di retry
     * @return
     */

    String sayHello(){
        log.info("Call sayHello method");
        throw new RuntimeException("ups error");
    }


    @Test
    void testRetryConfig() {

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(5)//berapa kali program akan di retry
                .retryExceptions(RuntimeException.class)//class error ini harus sama dengan error yang mungkin terjadi pada method yang akan di Retry
                .ignoreExceptions(IllegalArgumentException.class)//akan mengabaikan tipe error yang ada di method sayHello()
                .waitDuration(Duration.ofSeconds(2L))//waktu sebelum aksekusi retry
                .build();

        Retry retry = Retry.of("fjr",retryConfig);//"fjr" adalah nama retrynya, retryConfig adalah nama Confignya

        Supplier<String> stringSupplier = Retry.decorateSupplier(retry, this::sayHello);
        stringSupplier.get();

    }
}
