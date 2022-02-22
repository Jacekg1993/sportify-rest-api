package com.jacekg.teamfinder.venue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacekg.teamfinder.exceptions.ErrorResponse;
import com.jacekg.teamfinder.jwt.JwtAuthenticationEntryPoint;
import com.jacekg.teamfinder.jwt.JwtRequestFilter;
import com.jacekg.teamfinder.user.User;

@WebMvcTest(VenueRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class VenueRestControllerTest {
	
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private VenueService venueService;
	
	@MockBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@MockBean
	private UserDetailsService jwtUserDetailsService;

	@MockBean
	private JwtRequestFilter jwtRequestFilter;
	
	private VenueRequest venueRequest;
	
	private VenueResponse venueResponse;
	
	private Venue venue;
	
	private User user;
	
	private Point venueCoordinates;
	
	@BeforeEach
	void setUp() throws Exception {
		
		venueRequest = new VenueRequest("sport venue", "address 1", "sports hall");
		
		venueResponse = new VenueResponse("sport venue", "address 1", "sports hall");
		
		venue = new Venue(1L, "sport venue", "address 1",venueCoordinates, null, null);
		
		user = new User();
		user.setId(1L);
	}
	
	@Test
	void createVenue_ShouldReturn_StatusCreated_AndVenue() throws Exception {
		
		TestingAuthenticationToken testingAuthenticationToken = new TestingAuthenticationToken(user,null);
		
		String jsonBody = objectMapper.writeValueAsString(venueRequest);
		
		when(venueService.save(any(VenueRequest.class))).thenReturn(venueResponse);
		
		String url = "/v1/venues";
		
		MvcResult mvcResult = mockMvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
				.andExpect(status().isCreated()).andReturn();
		
		String returnedVenue = mvcResult.getResponse().getContentAsString();
		
		VenueResponse venueResponse = objectMapper.readValue(returnedVenue, VenueResponse.class);
		
		assertThat(venueResponse).hasFieldOrPropertyWithValue("name", "sport venue");
		assertThat(venueResponse).hasFieldOrPropertyWithValue("address", "address 1");
		assertThat(venueResponse).hasFieldOrPropertyWithValue("venueTypeName", "sports hall");
	}
	
	@Test
	void createVenue_ShouldThrow_MethodArgumentNotValidException() throws Exception {
		
		venueRequest.setAddress(null);
		
		String jsonBody = objectMapper.writeValueAsString(venueRequest);
		
		when(venueService.save(any(VenueRequest.class))).thenReturn(venueResponse);
		
		String url = "/v1/venues";
		
		MvcResult mvcResult = mockMvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
				.andExpect(status().isBadRequest()).andReturn();
		
		String responseContent = mvcResult.getResponse().getContentAsString();
		
		ErrorResponse errorResponse = objectMapper.readValue(responseContent, ErrorResponse.class);
		
		assertThat(errorResponse).hasFieldOrPropertyWithValue("message", "validation error");
	}
	
	@Test
	void findBySportDysciplineAndAddress_ShouldReturn_StatusOK_AndVenues() throws Exception {
		
		List<VenueResponse> venues = new ArrayList<VenueResponse>();
		venues.add(venueResponse);
		
		TestingAuthenticationToken testingAuthenticationToken = new TestingAuthenticationToken(user,null);
		
		when(venueService.findBySportDisciplineAndAddress(anyString(), anyString())).thenReturn(venues);
		
		String url = "/v1/venues/{sportDiscipline}/{address}";
		
		MvcResult mvcResult = mockMvc.perform(get(url, "football", "address 1")
				.principal(testingAuthenticationToken))
				.andExpect(status().isOk()).andReturn();
		
		String responseContent = mvcResult.getResponse().getContentAsString();
		
//		List<VenueResponse> venueResponses 
//			= (List<VenueResponse>) objectMapper.readValue(responseContent, VenueResponse.class);
		
		VenueResponse venueResponses 
			= objectMapper.readValue(responseContent, VenueResponse.class);
	}
}
