package org.example.fidstp2.controller;
import org.example.fidstp2.domain.Counterparty;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;
import org.example.fidstp2.service.CounterpartyQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
class CounterpartyControllerTest {
    @Test
    void returnsCounterpartyDetails() throws Exception {
        CounterpartyQueryService queryService = mock(CounterpartyQueryService.class);
        when(queryService.getById("CP-1")).thenReturn(new Counterparty("CP-1", "Demo Counterparty", true));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CounterpartyController(queryService)).build();
        mvc.perform(get("/api/reference/counterparties/CP-1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CP-1"))
                .andExpect(jsonPath("$.name").value("Demo Counterparty"))
                .andExpect(jsonPath("$.active").value(true));
    }
    @Test
    void returnsNotFoundWhenCounterpartyMissing() throws Exception {
        CounterpartyQueryService queryService = mock(CounterpartyQueryService.class);
        when(queryService.getById("CP-X")).thenThrow(new ReferenceDataNotFoundException("counterparty not found: CP-X"));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CounterpartyController(queryService)).build();
        mvc.perform(get("/api/reference/counterparties/CP-X").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("counterparty not found: CP-X"));
    }
}
