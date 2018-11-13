package tech.claudioed.continuous.service;

import javax.annotation.PostConstruct;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.claudioed.continuous.domain.Device;
import tech.claudioed.continuous.domain.Temperature;
import tech.claudioed.continuous.repository.DeviceRepository;
import tech.claudioed.continuous.repository.TemperatureRepository;

@Service
public class DeviceService {

  private final TemperatureRepository temperatureRepository;

  private final DeviceRepository deviceRepository;

  private ReactiveMongoTemplate reactiveMongoTemplate;

  public DeviceService(TemperatureRepository temperatureRepository,
      DeviceRepository deviceRepository,
      ReactiveMongoTemplate reactiveMongoTemplate) {
    this.temperatureRepository = temperatureRepository;
    this.deviceRepository = deviceRepository;
    this.reactiveMongoTemplate = reactiveMongoTemplate;
  }

  @PostConstruct
  public void init(){
    reactiveMongoTemplate.dropCollection("temperatures")
            .then(reactiveMongoTemplate.createCollection("temperatures", CollectionOptions.empty().capped().size(3221225472L)))
            .subscribe();
  }

  public Flux<Temperature> streamFromDevice(String id){
    return this.temperatureRepository.findByDeviceId(id);
  }

  public Flux<Temperature> streamJavaForDevice(String id) {
    return this.temperatureRepository.findAll().filter(temperature -> temperature.getDevice().getId().equals(id)||id==null);
  }

  public Flux<Temperature> streamAll() {
    return this.temperatureRepository.findWithTailableCursorBy();
  }

  public Mono<Device> newDevice(Device device){
    return this.deviceRepository.save(device);
  }

  public Flux<Temperature> reachMax(String deviceId){
    return this.deviceRepository.findById(deviceId)
        .flatMapMany(device -> this.temperatureRepository.findByDeviceIdAndValueGreaterThan(deviceId,device.getSetup().getMax()));
  }

  public Flux<Temperature> reachMin(String deviceId){
    return this.deviceRepository.findById(deviceId)
        .flatMapMany(device -> this.temperatureRepository.findByDeviceIdAndValueLessThan(deviceId,device.getSetup().getMin()));
  }

}
