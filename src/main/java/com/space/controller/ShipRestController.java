package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipRestController {
    @Autowired
    private ShipService shipService;

    @GetMapping("/ships")
    @ResponseBody
    public List<Ship> getShipsList(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "planet", required = false) String planet,
                                   @RequestParam(value = "shipType", required = false) ShipType shipType,
                                   @RequestParam(value = "after", required = false) Long after,
                                   @RequestParam(value = "before", required = false) Long before,
                                   @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                   @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                   @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                   @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                   @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                   @RequestParam(value = "minRating", required = false) Double minRating,
                                   @RequestParam(value = "maxRating", required = false) Double maxRating,
                                   @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
                                   @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                   @RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        return shipService.gelAllShips(
                Specification
                        .where(shipService.filterByName(name)
                                .and(shipService.filterByPlanet(planet)))
                        .and(shipService.filterByShipType(shipType))
                        .and(shipService.filterByDate(after, before))
                        .and(shipService.filterByUsage(isUsed))
                        .and(shipService.filterBySpeed(minSpeed, maxSpeed))
                        .and(shipService.filterByCrewSize(minCrewSize, maxCrewSize))
                        .and(shipService.filterByRating(minRating, maxRating)), pageable)
                .getContent();
    }

    @GetMapping("/ships/{id}")
    public @ResponseBody
    ResponseEntity<Ship> getShip(@PathVariable Long id) {
        Ship result = shipService.getShipById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ships/count")
    public @ResponseBody
    Long getShipsCount(@RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "planet", required = false) String planet,
                       @RequestParam(value = "shipType", required = false) ShipType shipType,
                       @RequestParam(value = "after", required = false) Long after,
                       @RequestParam(value = "before", required = false) Long before,
                       @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                       @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                       @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                       @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                       @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                       @RequestParam(value = "minRating", required = false) Double minRating,
                       @RequestParam(value = "maxRating", required = false) Double maxRating) {

        return shipService.getShipsCount(Specification.where(shipService.filterByName(name)
                .and(shipService.filterByPlanet(planet)))
                .and(shipService.filterByShipType(shipType))
                .and(shipService.filterByDate(after, before))
                .and(shipService.filterByUsage(isUsed))
                .and(shipService.filterBySpeed(minSpeed, maxSpeed))
                .and(shipService.filterByCrewSize(minCrewSize, maxCrewSize))
                .and(shipService.filterByRating(minRating, maxRating)));
    }


    @PostMapping("/ships/")
    @ResponseBody
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        shipService.createShip(ship);
        return ResponseEntity.ok(ship);
    }

    @PostMapping("/ships/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable Long id, @RequestBody Ship oldShip) {
        Ship newShip = shipService.updateShip(id, oldShip);
        return ResponseEntity.ok(newShip);
    }

    @DeleteMapping("/ships/{id}")
    public ResponseEntity<Ship> deleteShip(@PathVariable Long id) {
        shipService.deleteShipById(id);
        return ResponseEntity.ok().build();
    }
}

