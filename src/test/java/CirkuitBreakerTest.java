import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class CirkuitBreakerTest {


    /** CircuitBreaker
     * Circuit Breaker adalah implementasi dari finite state machine, dengan tiga normal state: CLOSED, OPEN dan HALF_OPEN, dan dua spesial state DISABLED dan FORCED_OPEN.
     * Kita bisa memilih menggunakan Circuit Breaker berbasis hitungan atau waktu
     * Basis hitungan berarti menghitung data berdasarkan jumlah N eksekusi terakhir
     * Basis waktu berarti menghitung data berdasarkan jumlah eksekusi dalam N detik terakhir
     * CLOSED -> circuit bersedia menerima request, ketika fault rate > thresshold maka circuit akan perpindah ke OPEN
     * OPEN -> circuit tidak bersedia menerima request, ketika sudah menunggu selama 1 menit circuit akan mencoba HALF-OPEN
     * HALF-OPEN -> circuit dapat menerima request namun kita batasi, untuk HALF-OPEN circuit akan menunggu setelah 50% gagal request saat status CLOSE, setelah menunggu 60 second, baru circuit akan masuk ke HALF-OPEN
     */

    void sayHello(){
        log.info("Sayy Heyyyy");
        throw new IllegalArgumentException("Error Say Hey");
    }


    @Test
    void testCircuitBreaker() {
        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("fjr");


        //defaultnya 100, dari 100 eksekusi terakhir kalo lebih dari 50% error maka dia otomatis statusnya open
        //ketika statunya berubah ke open otomatis circuit tidak dapat menerima requets lagi
        for (int i = 0; i < 300; i++) {

            try {
                Runnable runnable = CircuitBreaker.decorateRunnable(circuitBreaker, this::sayHello);
                runnable.run();
            }catch (Exception e){
                log.error("error : {}",e.getMessage());
            }


        }

    }

    /** CircuitBreakerConfig
     * Secara default, Circuit Breaker akan mencoba menghitung jumlah error rate setelah 100 kali eksekusi
     * Dan jika terjadi error diatas 50%, maka Circuit Breaker akan menjadi state OPEN, dan otomatis semua request akan ditolak dengan error CallNotPermittedException
     */


    /** CircuitBreakerConfig Mode
     * Saat kita membuat Circuit Breaker, kita bisa mengubah mode pengaturan berbasis hitungan atau waktu, defaultnya adalah menggunakan hitungan dengan jumlah minimal 100
     * Kita juga bisa mengubahnya menjadi mode waktu, sehingga error rate dihitung berdasarkan durasi waktu
     * Ada banyak sekali pengaturan yang bisa kita ubah pada Circuit Breaker
     */

    /** Pengaturan Circuit Breaker (1)
     * config:failureRateThreshold, defaultValue :50, Desc :Minimal persentase error rate agar state menjadi OPEN
     * config:slidingWindowType, defaultValue :COUNT_BASED, Desc:Tipe mode sliding window, count (hitung) atau time (durasi)
     * config:slidingWindowSize, defaultValue : 100, Decs:Jumlah sliding window yang di record pada waktu state CLOSED
     * config:minimumNumberOfCalls, defaultValue : 100, Decs:Jumlah minimal eksekusi sebelum error rate dihitung
     */

    /** Pengaturan Circuit Breaker (2)
     * config:waitDurationInOpenState  , defaultValue :60000 [ms], Desc :Waktu menunggu agar OPEN menjadi HALF_OPEN
     * config:permittedNumberOfCallsInHalfOpenState, defaultValue :10, Desc:Jumlah eksekusi yang diperbolehkan ketika Circuit Breaker pada state HALF_OPEN
     * config:maxWaitDurationInHalfOpenState, defaultValue : 0 ms, Decs:Jumlah maksimal menunggu di HALF_OPEN untuk kembali ke OPEN. 0 artinya menugggu tidak terbatas

     */


    /** Pengaturan Circuit Breaker (3)
     * config:slowCallDurationThreshold  , defaultValue :60000 [ms], Desc :Pengaturan sebuah eksekusi dianggap lambat
     * config:permittedNumberOfCallsInHalfOpenState, defaultValue :10, Desc:Jumlah eksekusi yang diperbolehkan ketika Circuit Breaker pada state HALF_OPEN
     * config:maxWaitDurationInHalfOpenState, defaultValue : 0 ms, Decs:Jumlah maksimal menunggu di HALF_OPEN untuk kembali ke OPEN. 0 artinya menugggu tidak terbatas

     */



    @Test
    void testCircuitBreakerConfig() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .failureRateThreshold(10f)//rata2 kesalahan 10%
                .slidingWindowSize(10)//default 100, sekarang diubah 10(jika error 10% dari 10)
                .minimumNumberOfCalls(10)
                .build();

        //awalnya program CLOSE menerima request yang masuk ketika hitungan eksekusi telah mencapai 10, lalu failed > threshold maka, program akan OPEN(tidak menerima request lagi)
        CircuitBreaker circuitBreaker = CircuitBreaker.of("fjr",config);


        //defaultnya 100, dari 100 eksekusi terakhir kalo lebih dari 50% error maka dia otomatis statusnya open
        //ketika statunya berubah ke open otomatis circuit tidak dapat menerima requets lagi
        for (int i = 0; i < 100; i++) {

            try {
                Runnable runnable = CircuitBreaker.decorateRunnable(circuitBreaker, this::sayHello);
                runnable.run();
            }catch (Exception e){
                log.error("error : {}",e.getMessage());
            }

        }

    }


    @Test
    void testCircuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .failureRateThreshold(10f)//rata2 kesalahan 10%
                .slidingWindowSize(10)//default 100, sekarang diubah 10(jika error 10% dari 10)
                .minimumNumberOfCalls(10)
                .build();

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        circuitBreakerRegistry.addConfiguration("config",config);

        //awalnya program CLOSE menerima request yang masuk ketika hitungan eksekusi telah mencapai 10, lalu failed > threshold maka, program akan OPEN(tidak menerima request lagi)
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("fjr","config");


        //defaultnya 100, dari 100 eksekusi terakhir kalo lebih dari 50% error maka dia otomatis statusnya open
        //ketika statunya berubah ke open otomatis circuit tidak dapat menerima requets lagi
        for (int i = 0; i < 100; i++) {

            try {
                Runnable runnable = CircuitBreaker.decorateRunnable(circuitBreaker, this::sayHello);
                runnable.run();
            }catch (Exception e){
                log.error("error : {}",e.getMessage());
            }

        }

    }





}
