import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

@Slf4j
public class EventPublisherTest {

    @Test
    void testEventPublisher() {


        /** Metric
         * Hampir semua module di Resilience4J memiliki fitur Metric, dimana kita bisa melihat data metric dari object yang sedang kita gunakan
         * Contohnya di Retry, kita bisa mendapatkan Metric data seberapa banyak eksekusi yang sukses dan gagal misalnya
         */


        /** Event Publisher
         * Semua module di Resilience4J memiliki fitur yang namanya adalah Event Publisher
         * Fitur ini digunakan untuk mengirim event kejadian ketika suatu kejadian terjadi
         * Contoh, kita bisa mendapatkan event ketika misal di Retry terjadi kejadian Sukses, Error atau Retry
         * Untuk mendapatkan Event Publisher object, kita bisa menggunakan getEventPublisher() di object seperti Retry, CircuitBreaker, RateLimiter, dan lain-lain
         * tidak hanya di Retry di RateLimiter, Bulkhead, bahkan di CircuitBreaker juga ada EventPublisher
         */


        Retry retry = Retry.ofDefaults("fjr");
        retry.getEventPublisher().onRetry(event -> log.info("Try to Retry"));

        try {
            Supplier<String> supplier = Retry.decorateSupplier(retry, this::hello);
            supplier.get();
        }catch (Exception e){
            System.out.println(retry.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt());
            System.out.println(retry.getMetrics().getNumberOfTotalCalls());
            System.out.println(retry.getMetrics().getNumberOfSuccessfulCallsWithoutRetryAttempt());
        }


    }


    private String hello(){
        throw new IllegalArgumentException("Ups error");
    }


    /** Event Publisher di Registry
     * untuk mengetahui kejadian ketika terjadi sesuatu dalam Registrynya
     * Semua Registry juga memiliki Event Publisher
     * Yang membedakan adalah, Event Publisher di Registry hanya digunakan untuk kejadian seperti menambah object, menghapus atau mengubah object  ke Registry
     */

    @Test
    void testEventPublisherRegistry() {
        RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
        retryRegistry.getEventPublisher().onEntryAdded(event -> {
            log.info("Add new Entry {}", event.getAddedEntry().getName());
//            event.getAddedEntry();//untuk mendapatkan Retry baru
        });

        retryRegistry.retry("fjr");//otomatis nambah retry jadi di lognya akan dipanggil
        retryRegistry.retry("fjr");// ketika manggil ini lagi log tidak akan dipanggil karna "fjr" sudah ada
        retryRegistry.retry("fjr2");//tapi ketika disini log akan dipanggil lagi karna ada penambahan retry
    }
}
