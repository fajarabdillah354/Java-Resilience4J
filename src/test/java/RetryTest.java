import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

@Slf4j
public class RetryTest {

    void sayHello(){
        log.info("try call me");
        throw new IllegalArgumentException("UPS ERROR");

    }

    @Test
    void testRetry() {
        //Dengan Retry kita bisa memanggil program secara lebih dari 1 dengan harapan jika terjadi error saat program dijalankan pertama maka saat dijalankan yang ke dua atau ke tiga bisa berjalan
        Retry retry =  Retry.ofDefaults("fajar");

        //akan mencoba memanggil sebanyak default 3x kali lalu akan menampilkan error jika program tidak berhasil dijalankan

        // setelah dibungkus dengan decorate selanjutnya kita akan kembalikaan ke nilai Runnable lalu kita panggil runnable dengan method run(), ini bagi yang tidak mengembalikan value. jika mengembalikan value menggunakan Supplier<T>, dimana type T adalah nilai kembaliannya
        Runnable runnable = Retry.decorateRunnable(retry, this::sayHello);
        runnable.run();

    }

    String hello(){
        log.info("Call say Hello ");
        throw new IllegalArgumentException("upps error!!!");
    }

    @Test
    void testRetryReturnValue() {

        Retry retry = Retry.ofDefaults("fjr");

        Supplier<String> supplier = Retry.decorateSupplier(retry, () -> hello());
        supplier.get();//untuk mendapatkan nilai kita menggunakan method get()


    }
}
