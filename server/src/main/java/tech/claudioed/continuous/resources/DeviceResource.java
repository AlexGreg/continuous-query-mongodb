package tech.claudioed.continuous.resources;

import java.util.function.Predicate;
import javax.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.claudioed.continuous.domain.Device;
import tech.claudioed.continuous.domain.Temperature;
import tech.claudioed.continuous.service.DeviceService;
import tech.claudioed.continuous.service.StreamProvider;

@RestController
@RequestMapping("/api/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final StreamProvider<Temperature> streamProvider;

    @PostConstruct
    public void init() {
        streamProvider.subscribe(deviceService.streamAll());
    }

    public DeviceResource(DeviceService deviceService, StreamProvider<Temperature> streamProvider) {
        this.deviceService = deviceService;
        this.streamProvider = streamProvider;
    }

    @PostMapping
    public Mono<Device> newDevice(@RequestBody Device device) {
        return this.deviceService.newDevice(device);
    }

    @GetMapping(value = "/{id}/max", produces = { MediaType.APPLICATION_STREAM_JSON_VALUE })
    public Flux<Temperature> reachMax(@PathVariable("id") String id) {
        return this.deviceService.reachMax(id);
    }

    @GetMapping(value = "/{id}/min", produces = { MediaType.APPLICATION_STREAM_JSON_VALUE })
    public Flux<Temperature> reachMin(@PathVariable("id") String id) {
        return this.deviceService.reachMin(id);
    }

    @GetMapping(value = "/{id}/stream", produces = { MediaType.APPLICATION_STREAM_JSON_VALUE })
    public Flux<Temperature> stream(@PathVariable("id") String id) {
        return this.streamProvider.getStream(temperature -> temperature.getDevice().getId().equals(id));
    }

    @GetMapping(value = "/stream", produces = { MediaType.APPLICATION_STREAM_JSON_VALUE })
    public Flux<Temperature> stream() {

            return streamProvider.getStream(null);
    }

}
