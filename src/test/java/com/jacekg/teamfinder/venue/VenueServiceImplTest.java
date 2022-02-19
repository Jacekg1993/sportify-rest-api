package com.jacekg.teamfinder.venue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.jacekg.teamfinder.geocoding.GeocodingService;
import com.jacekg.teamfinder.geocoding.model.GeocodeGeometry;
import com.jacekg.teamfinder.geocoding.model.GeocodeLocation;
import com.jacekg.teamfinder.geocoding.model.GeocodeObject;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {
	
	@InjectMocks
	VenueServiceImpl serviceUnderTest;
	
	@Mock
	private GeocodingService geocodingService;
	
	@Mock
	private VenueRepository venueRepository;
	
	@Mock
	private VenueTypeRepository venueTypeRepository;
	
	@Mock
	private ModelMapper modelMapper;
	
	@Mock
	private GeometryFactory geometryFactory;
	
	private Venue venue;
	
	private VenueRequest venueRequest;
	
	private VenueResponse venueResponse;
	
	private GeocodeObject geocodeObject;
	
	private VenueType venueType;
	
	private Point venueCoordinates;
	
	@BeforeEach
	void setUp() {
		
		venueRequest = new VenueRequest("sport venue", "address 1", "sports hall");
		
		venueResponse = new VenueResponse("sport venue", "address 1", "sports hall");

		venueType = new VenueType(1L, "sports hall");
		
		venue = new Venue(1L, "sport venue", "address 1", venueCoordinates, venueType, null);
		
		GeocodeLocation geocodeLocation = new GeocodeLocation(1.0, 1.0); 

		GeocodeGeometry geometry = new GeocodeGeometry(geocodeLocation);
		
		geocodeObject = new GeocodeObject("1a", "address 1", geometry);
	}
	
	@Test
	void save_ShouldReturn_Venue() throws IOException {
		
		when(geocodingService.findLocationByAddress(Mockito.anyString())).thenReturn(geocodeObject);
		when(geometryFactory.createPoint(Mockito.any(Coordinate.class))).thenReturn(null);
		when(venueTypeRepository.findByName(Mockito.anyString())).thenReturn(venueType);
		when(venueRepository.findByLocationAndVenueType(venueCoordinates, venueType)).thenReturn(null);
		when(venueRepository.save(Mockito.any(Venue.class))).thenReturn(venue);
		when(modelMapper.map(venueRequest, Venue.class)).thenReturn(venue);
		when(modelMapper.map(venue, VenueResponse.class)).thenReturn(venueResponse);
		
		VenueResponse savedVenue = serviceUnderTest.save(venueRequest);
		
		verify(venueRepository).save(Mockito.any(Venue.class));
		
		assertThat(savedVenue).hasFieldOrPropertyWithValue("name", "sport venue");
		assertThat(savedVenue).hasFieldOrPropertyWithValue("address", "address 1");
		assertThat(savedVenue).hasFieldOrPropertyWithValue("venueTypeName", "sports hall");
	}
}






