package com.bruin.reactor;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Time;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: xiongwenwen   2020/4/21 16:16
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {

//        Flux<Integer> flux = Flux.just(1,2,3,4,5,6);
//        flux.map(i -> i * i).subscribe(
//                System.out::println,
//                System.err::println,
//                () -> System.out.println("completed"));
//
//        Flux.just("hello","flux","mono","spring").flatMap(i -> Flux.fromArray(i.split("\\s*")).delayElements(Duration.ofMillis(100)))
//                .doOnNext(System.out::print);
//
//        Flux.range(1, 6).filter(i -> i > 3).map(i -> i * i).subscribe(System.out::println);

        zipWith();
    }

    public static void zip() throws InterruptedException {
        String str = "Zip two sources together, that is to say wait for all the sources to emit one element and combine these elements once into a Tuple2.";

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Flux.zip(Flux.fromArray(str.split("\\s+")),
                Flux.interval(Duration.ofMillis(200))).subscribe(t -> System.out.println(t.getT1()), null, countDownLatch::countDown);

        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    public static void zipWith() throws InterruptedException {
        String str = "Zip two sources together, that is to say wait for all the sources to emit one element and combine these elements once into a Tuple2.";

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Flux.fromArray(str.split("\\s+")).zipWith(Flux.interval(Duration.ofMillis(200)))
                .subscribe(t -> System.out.println(t.getT1()), null, countDownLatch::countDown);

        countDownLatch.await(10, TimeUnit.SECONDS);
    }
}
