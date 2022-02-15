package com.example.springreativeflux.domain;

import com.example.springreativeflux.DBinit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import(DBinit.class) // DB 데이터 필요해서
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void 한건찾기_테스트() {
        StepVerifier
                .create(customerRepository.findById(2L))
                .expectNextMatches((c)-> {
                    return c.getFirstName().equals("Chloe");
                })
                .expectComplete()
                .verify();
    }
}
