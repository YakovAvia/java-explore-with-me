package ru.practicum.main.location.mapper;

import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.model.Location;

public class LocationMapper {

    public static Location toLocation(LocationDto locationDto) {
        return new Location(
                null,
                locationDto.getLat(),
                locationDto.getLon()
        );
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(
                location.getLat(),
                location.getLon()
        );
    }
}
