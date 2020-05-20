package com.space.service;


import com.space.exception.BadRequestException;
import com.space.exception.ShipNotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;


@Service
public class ShipServiceImpl implements ShipService {
    @Autowired
    private ShipRepository shipRepository;

    private static final int LEFT_YEAR = 2800;
    private static final int RIGHT_YEAR = 3019;
    private static final int VARCHAR_LENGTH = 50;

    @Override
    public Page<Ship> gelAllShips(Specification<Ship> specification, Pageable pageable) {
        return shipRepository.findAll(specification, pageable);
    }

    public Long getShipsCount(Specification<Ship> specification) {
        return shipRepository.count(specification);
    }

    @Override
    public Ship getShipById(Long id) {
        validateId(id);

        return shipRepository.findById(id).orElseThrow(() ->
                new ShipNotFoundException(String.format("Ship with id %d not found!", id)));
    }

    @Override
    public void deleteShipById(Long id) {
        Ship ship = getShipById(id);
        shipRepository.delete(ship);
    }

    @Override
    public Ship createShip(Ship ship) {
        validateName(ship.getName());
        validatePlanet(ship.getPlanet());
        validateShipType(ship.getShipType());
        validateProdDate(ship.getProdDate());
        validateSpeed(ship.getSpeed());
        validateCrewSize(ship.getCrewSize());

        if (ship.getUsed() == null)
            ship.setUsed(false);

        ship.setRating(calculateRating(ship.getProdDate().getTime(), ship.getSpeed(), ship.getUsed()));

        return shipRepository.saveAndFlush(ship);
    }

    @Override
    public Ship updateShip(Long id, Ship newShip) {
        Ship old = getShipById(id);

        if (newShip.getName() != null) {
            validateName(newShip.getName());
            old.setName(newShip.getName());
        }

        if (newShip.getPlanet() != null) {
            validatePlanet(newShip.getPlanet());
            old.setPlanet(newShip.getPlanet());
        }

        if (newShip.getShipType() != null) {
            validateShipType(newShip.getShipType());
            old.setShipType(newShip.getShipType());
        }

        if (newShip.getProdDate() != null) {
            validateProdDate(newShip.getProdDate());
            old.setProdDate(newShip.getProdDate());
        }

        if (newShip.getUsed() != null) {
            old.setUsed(newShip.getUsed());
        }

        if (newShip.getSpeed() != null) {
            validateSpeed(newShip.getSpeed());
            old.setSpeed(newShip.getSpeed());
        }

        if (newShip.getCrewSize() != null) {
            validateCrewSize(newShip.getCrewSize());
            old.setCrewSize(newShip.getCrewSize());
        }

        old.setRating(calculateRating(old.getProdDate().getTime(), old.getSpeed(), old.getUsed()));
        shipRepository.save(old);
        return old;
    }

    @Override
    public Specification<Ship> filterByName(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(root.get("name"), "%" + name + "%");
    }

    @Override
    public Specification<Ship> filterByPlanet(String planet) {
        return (root, query, cb) -> planet == null ? null : cb.like(root.get("planet"), "%" + planet + "%");
    }

    @Override
    public Specification<Ship> filterByShipType(ShipType shipType) {
        return (root, query, cb) -> shipType == null ? null : cb.equal(root.get("shipType"), shipType);
    }

    @Override
    public Specification<Ship> filterByDate(Long after, Long before) {
        return (root, query, cb) -> {
            if (after == null && before == null) {
                return null;
            }

            if (after == null) {
                return cb.lessThanOrEqualTo(root.get("prodDate"), new Date(before));
            }

            if (before == null) {
                return cb.greaterThanOrEqualTo(root.get("prodDate"), new Date(after));
            }

            return cb.between(root.get("prodDate"), new Date(after), new Date(before));
        };
    }

    @Override
    public Specification<Ship> filterByUsage(Boolean isUsed) {
        return (root, query, cb) -> {
            if (isUsed == null) {
                return null;
            }

            if (isUsed) {
                return cb.isTrue(root.get("isUsed"));
            }

            return cb.isFalse(root.get("isUsed"));
        };
    }

    @Override
    public Specification<Ship> filterBySpeed(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }

            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("speed"), max);
            }

            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("speed"), min);
            }

            return cb.between(root.get("speed"), min, max);
        };
    }

    @Override
    public Specification<Ship> filterByCrewSize(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }

            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("crewSize"), max);
            }

            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("crewSize"), min);
            }

            return cb.between(root.get("crewSize"), min, max);
        };
    }

    @Override
    public Specification<Ship> filterByRating(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }

            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("rating"), max);
            }

            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("rating"), min);
            }

            return cb.between(root.get("rating"), min, max);
        };
    }

    private double calculateRating(Long prodDate, Double speed, Boolean isUsed) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(prodDate);
        int year = calendar.get(Calendar.YEAR);

        BigDecimal raiting = BigDecimal.valueOf((80 * speed * (isUsed ? 0.5 : 1)) / (3019 - year + 1));

        raiting = raiting.setScale(2, RoundingMode.HALF_UP);
        return raiting.doubleValue();
    }

    private void validateId(Long id) {
        if (id <= 0) {
            throw new BadRequestException("Ship id is invalid!");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isEmpty() || name.length() > VARCHAR_LENGTH)
            throw new BadRequestException("Wrong ship name!");
    }

    private void validatePlanet(String planet) {
        if (planet == null || planet.isEmpty() || planet.length() > VARCHAR_LENGTH)
            throw new BadRequestException("Wrong ship planet!");
    }

    private void validateShipType(ShipType shipType) {
        if (shipType == null)
            throw new BadRequestException("Wrong ship type!");
    }

    private void validateProdDate(Date prodDate) {
        if (prodDate == null)
            throw new BadRequestException("Wrong prod date!");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(prodDate.getTime());
        if (calendar.get(Calendar.YEAR) < LEFT_YEAR || calendar.get(Calendar.YEAR) > RIGHT_YEAR)
            throw new BadRequestException("ProdDate is out of bounds!");
    }

    private void validateSpeed(Double speed) {
        if (speed == null || speed < 0.01D || speed > 0.99D)
            throw new BadRequestException("Wrong speed!");
    }

    private void validateCrewSize(Integer crewSize) {
        if (crewSize == null || crewSize < 1 || crewSize > 9999)
            throw new BadRequestException("Wrong crew size!");
    }

}
