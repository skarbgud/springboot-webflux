package com.example.springreativeflux.web;

import com.example.springreativeflux.domain.Customer;
import com.example.springreativeflux.domain.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RestController
public class CustomerController {

    private final CustomerRepository customerRepository;

    /*
    sink란?

    A요청 -> FLUX -> Stream
    B요청 -> FLUX -> Stream

    두개의 Steam을 Merge 해줌
     */
    private final Sinks.Many<Customer> sink;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @GetMapping("flux")
    public Flux<Integer> flux() {
        // just는 데이터들을 Subscribe해서 순차적으로 반환해줌 (한번에)
        return Flux.just(1,2,3,4,5).delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(value = "fluxstream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Integer> fluxstream() {
        // onNext 할때 마다 순차적으로 Flush 해준다.
        return Flux.just(1,2,3,4,5).delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping("/customer")
    public Flux<Customer> findAll() {
        // DB에서 조회할때 onSubscribe()로 구독 정보를 가져온다.
        // unbounded 모두 조회
        // onNext로 하나씩 조회
        // onComplete()되는 순간 응답한다.
        return customerRepository.findAll().log();
    }

    @GetMapping(value = "/customerDelay", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Customer> findAllDelay() {
        // 모두 데이터 조회를 딜레이를 걸어서 가져오기
        return customerRepository.findAll().delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping("/customer/{id}")
    public Mono<Customer> findById(@PathVariable Long id) { // Mono는 1개만 조회할때 (onNext를 한번만)
        return customerRepository.findById(id).log();
    }

    // MediaType.TEXT_EVENT_STREAM_VALUE라는 표준이 있음
    @GetMapping(value = "/customer/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // sse -> 연결이 계속 유지되는
    public Flux<Customer> findAllSSE() {
        return customerRepository.findAll().delayElements(Duration.ofSeconds(1)).log();
        /*
        출력 결과 => data가 앞에 붙는다.
        data:{"id":1,"firstName":"Jack","lastName":"Bauer"}

        data:{"id":2,"firstName":"Chloe","lastName":"O'Brian"}

        data:{"id":3,"firstName":"Kim","lastName":"Bauer"}

        data:{"id":4,"firstName":"David","lastName":"Palmer"}

        data:{"id":5,"firstName":"Michelle","lastName":"Dessler"}

        데이터가 다 던지고 나면 멈춘다.
         */
    }

    /*
        비동기 단일 쓰레드로 동작
     */
    @GetMapping(value = "/customer/sseSink") // Flux<ServerSentEvent<Customer>>기 때문에  produces = MediaType.TEXT_EVENT_STREAM_VALUE 생략
    public Flux<ServerSentEvent<Customer>> findAllSSESink() {
        // 합쳐진 데이터를 응답
        // 들어온 데이터가 없어서 계속 대기
        // sink후 데이터를 push 해야함
        return sink.asFlux().map(c-> ServerSentEvent.builder(c).build()).doOnCancel(() -> {
            // 마지막 데이터를 받고 sink를 닫아서 막음
            sink.asFlux().blockLast();
        });
    }

    @PostMapping("/customer")
    public Mono<Customer> save() {
        return customerRepository.save(new Customer("gildong", "Hong")).doOnNext(c -> {
            // c 데이터를 emit해서 데이터를 추가해서 즉각적으로 /customer/sseSink에서 반응함
            sink.tryEmitNext(c);
        });
        /*
        결과값
        {
            "id": 6,
            "firstName": "gildong",
            "lastName": "Hong"
        }
         */
    }
}
