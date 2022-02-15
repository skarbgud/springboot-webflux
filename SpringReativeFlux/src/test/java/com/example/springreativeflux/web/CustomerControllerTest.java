package com.example.springreativeflux.web;

import com.example.springreativeflux.domain.Customer;
import com.example.springreativeflux.domain.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;


// 통합 테스트
//@SpringBootTest
//@AutoConfigureWebTestClient
@WebFluxTest // 컨트롤러만 올리는 것
public class CustomerControllerTest {

    @MockBean // 가짜 MockBean을 스프링 컨테이너에 생성
    CustomerRepository customerRepository;

//    @Autowired
//    CustomerRepository customerRepository;

    @Autowired
    private WebTestClient webTestClient; // 비동기로 http 요청


    @Test
    public void 한건찾기_테스트() {
//        Flux<Customer> fCustomer = customerRepository.findAll();
//        fCustomer.subscribe((customer) -> System.out.println("데이터" + '\n' + customer));

        // given
        Mono<Customer> givenData = Mono.just(new Customer("Jack", "Bauer"));


        // stub -> 행동 지시
        when(customerRepository.findById(1L)).thenReturn(givenData);
        webTestClient.get().uri("/customer/{id}", 1L)
                .exchange()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Jack")
                .jsonPath("$.lastName").isEqualTo("Bauer");
    }
}
