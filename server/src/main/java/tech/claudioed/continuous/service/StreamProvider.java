package tech.claudioed.continuous.service;

import java.util.function.Predicate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;


@Component
public class StreamProvider<T> {
    private EmitterProcessor<T> emitterProcessor;


    public StreamProvider() {
        emitterProcessor = EmitterProcessor.create();
        emitterProcessor.subscribe();

    }

    public void subscribe(Flux<T> flux) {
        flux.doOnNext(t -> emitterProcessor.onNext(t))
                .doOnComplete(() -> {
                    System.out.println("Flux is empty. Retrying connection");
                    subscribe(flux);
                })
                .doOnError(throwable -> {
                    subscribe(flux);
                })
                .subscribe();
    }

    public Flux<T> getStream(Predicate<T> predicate) {
        return predicate!=null?emitterProcessor.filter(predicate):emitterProcessor;
    }


}
