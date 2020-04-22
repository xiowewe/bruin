package com.bruin.reactor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

//@SpringBootTest
class BruinStudyReactorApplicationTests {

    @Test
    void contextLoads() {
        StepVerifier.create(Flux.just(1,2,3,4,5,6))
                .expectNext(1,2,3,4,5,6)
                .expectComplete()
                .verify();

        StepVerifier.create(Mono.error(new Exception("error")))
                .expectErrorMessage("error")
                .verify();

        StepVerifier.create(Flux.just("flux","mono").flatMap(s -> Flux.fromArray(s.split("\\s*"))
                .delayElements(Duration.ofMillis(100))).doOnNext(System.out::print)).expectNextCount(8).verifyComplete();

    }

}
