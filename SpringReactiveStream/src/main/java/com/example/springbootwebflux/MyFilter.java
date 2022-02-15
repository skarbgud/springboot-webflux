package com.example.springbootwebflux;


import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MyFilter implements Filter {

    private EventNotify eventNotify;

    public MyFilter(EventNotify eventNotify) {
        this.eventNotify = eventNotify;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("필터 실행됨");

        HttpServletResponse servletResponse = (HttpServletResponse) response;
        servletResponse.setContentType("text/event-stream;charset=utf-8");

        PrintWriter out = servletResponse.getWriter();
        // 1. Reactive Streams 라이브러리를 쓰면 표준을 지켜서 응답할 수 있다.
        for (int i = 0; i < 5; i++) {
            out.print("응답: " + i + "\n");
            out.flush(); // 버퍼 비우기
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 2. SSE Emitter 라이브러리를 사용하면 편하게 쓸 수 있다.
        while (true) {
            try {
                if (eventNotify.getChange()) {
                    int lastIndex = eventNotify.getEvents().size() - 1;
                    out.print("응답: " + eventNotify.getEvents().get(lastIndex) + "\n");
                    out.flush();
                    eventNotify.setChange(false);
                }
                Thread.sleep(1);
            } catch (InterruptedException e)     {
                e.printStackTrace();
            }
        }

        // 3.WebFlux => Reactive Streams 가 적용된 stream 을 배우고(비동기 단일스레드 동작)
        // 4.Servlet MVC => Reactive Streams 가 적용된 stream 을 배우고(멀티 스레드 방식)
    }
}
