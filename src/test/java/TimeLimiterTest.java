import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class TimeLimiterTest {

    /** TimeLimiter
     * Time Limiter merupakan module di Resilience4J yang digunakan untuk membatasi durasi dari sebuah eksekusi kode program
     * Dengan Time Limiter, kita bisa menentukan berapa maksimal durasi eksekusi sebuah kode program, jika melebihi yang sudah ditentukan, secara otomatis eksekusi tersebut akan dibatalkan dan akan terjadi error
     * Time Limiter membutuhkan eksekusi dalam bentuk Future atau Completable Future
     * @return
     */


    @SneakyThrows
    String sayHello(){
        log.info("heyyyy");
        Thread.sleep(2000L);
        return "Fajar";
    }


    @Test
    void testTimeLimiter() throws Exception {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> sayHello());

        TimeLimiter timeLimiter = TimeLimiter.ofDefaults("fjr");
        Callable<String> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);

        callable.call();

        //karna default time limitnya 1 detik diatas 1 detik tidak ditunggu,sementar pada method sayHello() waktu tunggunya selama 5 detik maka terjadi error
    }


    /** TimeLimiterConfig
     * Secara default, Time Limiter akan menunggu sekitar 1 detik sampai dianggap timeout
     * Namun kita juga bisa mengubah pengaturan nya, dengan menggunakan Time Limiter Config
     * @throws Exception
     */
    @Test
    void testTimeLimiterConfig() throws Exception {

        /** Pengaturan Time Limiter
         * timeoutDuration, default:1s -> Durasi ekskusi proses ditunggu sampai dianggap timeout
         * cancelRunningFuture, default:true -> Apakah future dibatalkan jika terjadi timeout
         */

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> sayHello());

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();


        TimeLimiter timeLimiter = TimeLimiter.of("fjr", timeLimiterConfig);
        Callable<String> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);

        callable.call();


    }


    @Test
    void testTimeLimiterRegistry() throws Exception {

        /** Pengaturan TimeLimiter
         * timeoutDuration, default:1s -> Durasi ekskusi proses ditunggu sampai dianggap timeout
         * cancelRunningFuture, default:true -> Apakah future dibatalkan jika terjadi timeout
         * DISARANKAN MENGGUNAKAN TimeLimiterRegistry
         */

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> sayHello());

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();

        //Registry
        TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.ofDefaults();
        timeLimiterRegistry.addConfiguration("config", timeLimiterConfig);

        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("fjr","config");
        Callable<String> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);

        callable.call();


    }


}
