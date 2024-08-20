import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class RateLimiterTest {//RateLimiter membatasi jumlah eksekusinya, TimeLimiter membatasi jumlah waktu eksekusinya

    /** RateLimiter
     * Rate Limiter merupakan module di Resilience4J yang bisa digunakan untuk membatasi jumlah eksekusi pada waktu tertentu
     * Rate Limiter sering sekali digunakan ketika misal kita tidak ingin terlalu banyak request yang diterima untuk menjalankan sebuah kode program, dengan demikian kita bisa memastikan program kita tidak terbebani terlalu berat
     * Jika request sudah melebihi batas waktu yang sudah ditentukan, secara otomatis Rate Limiter akan menjadikan request tersebut error dengan exception RequestNotPermitted
     */

    private final AtomicLong counter = new AtomicLong(0L);
    @Test
    void testRateLimiter() {
        RateLimiter rateLimiter = RateLimiter.ofDefaults("fjr");

        for (int i = 0; i < 10_000; i++) {

            Runnable runnable = RateLimiter.decorateRunnable(rateLimiter, () -> {
                long result = counter.incrementAndGet();
                log.info("Result : {}", result);
            });

            //ini akan tetap berjalan semua, padahal kita menggunakan RateLimiter untuk membatasi agar tidak semua dijalankan, kita perlu mengatur Config dari RateLimiter
            runnable.run();

        }
    }


    /** RateLimiterConfig
     * Rate Limiter memiliki config yang bisa kita atur sesuai dengan kebutuhan kita
     * Kita bisa membuat config menggunakan RateLimiterConfig
     *
     Pengaturan RateLimeter
     1. limitForPeriod : default(50) -> Jumlah yang diperbolehkan dalam periode refresh
     2. limitRefreshPeriod : default(500[nanosecond]) -> Durasi refresh, setelah mencapai waktu refresh, limit akan kembali ke nol
     3. timeoutDuration : default(5[second]) -> Waktu maksimal menunggu rate limiter
     */

    @Test
    void testRateLimiterConfig() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100)//MEMBATASI jika lebih dari 100 maka akan di reject
                .limitRefreshPeriod(Duration.ofMinutes(2))//Durasi reflesh , setelah mencapai waktu reflesh , limit akan kembali ke nol(0)
                .timeoutDuration(Duration.ofSeconds(2))//waktu eksekusi jika lebih dari 2 detik maka dinyatakan reject
                .build();


        RateLimiter rateLimiter = RateLimiter.of("fjr",config);

        for (int i = 0; i < 10_000; i++) {

            //ketika program sampai pada perulangan 100 dari 10_000 maka akan berhenti dan akan kembali ke 0, dan setelah 2 detik jika tidak berjalan lagi akan timeoutDuration

            Runnable runnable = RateLimiter.decorateRunnable(rateLimiter, () -> {
                long result = counter.incrementAndGet();
                log.info("Result : {}", result);
            });

            //ini akan tetap berjalan semua, padahal kita menggunakan RateLimiter untuk membatasi agar tidak semua dijalankan, kita perlu mengatur Config dari RateLimiter
            runnable.run();

        }
    }


    /** RateLimiterRegistry
     * Sama seperti Retry, Rate Limiter pun memiliki Registry untuk melakukan management object Rate Limiter
     * Dan sebaiknya saat membuat aplikasi, kita menggunakan Registry untuk melakukan manajemen object Rate Limiter nya
     */

    @Test
    void testRateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100)//MEMBATASI jika lebih dari 100 maka akan di reject
                .limitRefreshPeriod(Duration.ofMinutes(2))//Durasi reflesh , setelah mencapai waktu reflesh , limit akan kembali ke nol(0)
                .timeoutDuration(Duration.ofSeconds(2))//waktu eksekusi jika lebih dari 2 detik maka dinyatakan reject
                .build();


        //jangan buat langsung ratelimiter, tapi lebih bagus gunakan registry dahulu
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
        rateLimiterRegistry.addConfiguration("config", config);

        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("fjr", "config");

        for (int i = 0; i < 10_000; i++) {

            //ketika program sampai pada perulangan 100 dari 10_000 maka akan berhenti dan akan kembali ke 0, dan setelah 2 detik jika tidak berjalan lagi akan timeoutDuration

            Runnable runnable = RateLimiter.decorateRunnable(rateLimiter, () -> {
                long result = counter.incrementAndGet();
                log.info("Result : {}",result);
            });

            //ini akan tetap berjalan semua, padahal kita menggunakan RateLimiter untuk membatasi agar tidak semua dijalankan, kita perlu mengatur Config dari RateLimiter
            runnable.run();

        }
    }



}
