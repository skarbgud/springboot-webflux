package com.example.reactive;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class MySub implements Subscriber<Integer> {

    private Subscription s;
    private int bufferSize = 3;

    @Override
    public void onSubscribe(Subscription s) {
        System.out.println("구독자: 구독 정보 잘 받았어");
        this.s = s;
        System.out.println("구독자: 나 이제 신문 1개씩 줘!");
        s.request(12); // 신문 한개 매일 매일 줘! (백프레셔) 소비자가 한번에 처리할 수 있는 개수를 요청
    }

    @Override
    public void onNext(Integer integer) {
        System.out.println("on next(): " + integer);
        bufferSize--;
        if (bufferSize == 0) {
            System.out.println("하루 지남");
            bufferSize = 3;
            s.request(bufferSize);
        }
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("구독중 에러");
    }

    @Override
    public void onComplete() {
        System.out.println("구독완료");
    }
}
