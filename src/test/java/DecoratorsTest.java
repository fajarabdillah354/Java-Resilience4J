import com.sun.jdi.request.DuplicateRequestException;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
public class DecoratorsTest {


    /** Decorators
     * Pada kasus tertentu, kadang kita ingin menggabungkan beberapa module di Resilience4J secara sekaligus
     * Misal kita ingin menggabungkan Circuit Breaker dan Retry
     * Resilience4J menyediakan module tambahan bernama Decorators, dimana kita bisa menggabungkan beberapa module secara bersamaan
     * Namun sampai saat ini, Decorators belum bisa untuk menggabungkan module Time Limiter
     * @return
     */

    @SneakyThrows
    void sayHello(){
        log.info("Hellloooooowwww");
        Thread.sleep(2_000L);//ketika sudah menampilkan log.info akan menunggu selama 2 detik
        throw new IllegalArgumentException("ERROR");
    }


    @SneakyThrows
    String saySomething(){
        log.info("Say Something");
        Thread.sleep(1_000L);//ketika sudah menampilkan log.info akan menunggu selama 2 detik
        throw new IllegalArgumentException("ERROR");
    }

    @Test
    void testDecorators() throws InterruptedException {

        //Membuat RateLimiter
        RateLimiter rateLimiter = RateLimiter.of("fjr-rateLimiter", RateLimiterConfig.custom()
                .limitForPeriod(5)//membuat limit 5 pada eksekusi
                .limitRefreshPeriod(Duration.ofMinutes(1))//limit reflesh
                .build()
        );

        //Membuat Retry
        Retry retry = Retry.of("fjr-retry", RetryConfig.custom()
                .maxAttempts(10)//akan Retry jika sudah ada 10 proses
                .waitDuration(Duration.ofSeconds(1))//waktu tunggu
                .build()
        );

        //Decorators with Runnable
        Runnable runnable = Decorators.ofRunnable(() -> sayHello())
                .withRetry(retry)
                .withRateLimiter(rateLimiter)
                .decorate();

        // RateLimiter program akan running setiap 5 eksekusi lalu jika sudah mencapai maxAttempts(10) yang ada di Retry, lalu akan terkena permit yang ada di Retry
        for (int i = 0; i < 100; i++) {
            new Thread(runnable).start();

        }

        Thread.sleep(10_000L);

    }


    /** Fallback
     * Untuk kasus dimana functional interface nya bisa mengembalikan value, maka kita bisa menambah fallback di dalam Decorators
     * Artinya, jika ternyata terjadi error ketika melakukan eksekusi, maka secara otomatis function fallback akan dipanggil
     * @throws InterruptedException
     */

    @Test
    void testFallback() throws InterruptedException {

        //Membuat RateLimiter
        RateLimiter rateLimiter = RateLimiter.of("fjr-rateLimiter", RateLimiterConfig.custom()
                .limitForPeriod(5)//membuat limit 5 pada eksekusi
                .limitRefreshPeriod(Duration.ofMinutes(1))//limit reflesh
                .build()
        );

        //Membuat Retry
        Retry retry = Retry.of("fjr-retry", RetryConfig.custom()
                .maxAttempts(10)//akan Retry jika sudah ada 10 proses
                .waitDuration(Duration.ofSeconds(1))//waktu tunggu
                .build()
        );


        Supplier<String> supplier = Decorators.ofSupplier(() -> saySomething())
                .withRetry(retry)
                .withRateLimiter(rateLimiter)
                .withFallback(throwable -> "===== FAILED ======")//jika saat running tetap gagal maka program baliknya ke fallback dan akan menampilkan Throwble
                .decorate();

        System.out.println(supplier.get());

    }







}
