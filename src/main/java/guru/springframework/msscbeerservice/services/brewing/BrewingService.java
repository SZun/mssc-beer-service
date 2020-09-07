package guru.springframework.msscbeerservice.services.brewing;

import guru.springframework.msscbeerservice.config.JmsConfig;
import guru.springframework.msscbeerservice.domain.Beer;
import guru.springframework.msscbeerservice.events.BrewBeerEvent;
import guru.springframework.msscbeerservice.repositories.BeerRepository;
import guru.springframework.msscbeerservice.services.inventory.BeerInventoryService;
import guru.springframework.msscbeerservice.web.mappers.BeerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrewingService {

    private final BeerRepository beerRepository;
    private final BeerInventoryService beerInventoryService;
    private final BeerMapper beerMapper;
    private final JmsTemplate jmsTemplate;

    @Scheduled(fixedRate = 5000)
    public void checkLowInventory(){
        List<Beer> beers = beerRepository.findAll();

        beers.forEach(i -> {
            Integer invQOH = beerInventoryService.getOnhandInventory(i.getId());
            Integer minOnHand = i.getMinOnHand();

            log.debug("Min on Hand -> {}", minOnHand);
            log.debug("Inventory is -> {}", invQOH);

            if(minOnHand >= invQOH) {
                jmsTemplate.convertAndSend(JmsConfig.BREWING_REQUEST_QUEUE,
                        new BrewBeerEvent(beerMapper.beerToBeerDto(i)));
            }
        });
    }

}
