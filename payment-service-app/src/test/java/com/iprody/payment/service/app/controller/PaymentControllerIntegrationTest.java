package com.iprody.payment.service.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iprody.payment.service.app.AbstractPostgresIntegrationTest;
import com.iprody.payment.service.app.TestJwtFactory;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.persistence.service.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentNoteDto;
import com.iprody.payment.service.app.persistence.service.dto.PaymentStatusDto;
import com.iprody.payment.service.app.persistency.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static com.iprody.payment.service.app.persistence.entity.PaymentStatus.NOT_SENT;
import static com.iprody.payment.service.app.services.PaymentServiceImpl.PAYMENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Import(PaymentControllerIntegrationTest.TestClockConfig.class)
public class PaymentControllerIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String STRING_FIXED_INSTANT = "2026-03-03T12:15:55Z";
    private static final Instant FIXED_INSTANT = Instant.parse(STRING_FIXED_INSTANT);

    @TestConfiguration
    static class TestClockConfig {
        @Primary
        @Bean
        Clock clock() {
            return Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;


    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void shouldReturnOnlyLiquibasePayments() throws Exception {
        // given - when
        mockMvc.perform(
                        get("/payments/search")
                                .with(
                                        TestJwtFactory.jwtWithRole("test-user", "reader")
                                )
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "createdAt")
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.guid=='00000000-0000-0000-0000-000000000001')]").exists())
                .andExpect(jsonPath("$.content[?(@.guid=='00000000-0000-0000-0000-000000000002')]").exists())
                .andExpect(jsonPath("$.content[?(@.guid=='00000000-0000-0000-0000-000000000003')]").exists());


    }

    @Test
    void shouldCreatePaymentAndVerifyInDatabase() throws Exception {
        // given
        PaymentDto dto = new PaymentDto();
        dto.setAmount(new BigDecimal("123.45"));
        dto.setCurrency("EUR");
        dto.setStatus(PaymentStatus.PENDING);
        dto.setInquiryRefId(UUID.randomUUID());

        String json = objectMapper.writeValueAsString(dto);

        // when - then
        String response =
                mockMvc.perform(
                                post("/payments")
                                        .with(
                                                TestJwtFactory.jwtWithRole("test-user", "admin")
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json)
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.guid").exists())
                        .andExpect(jsonPath("$.currency").value("EUR"))
                        .andExpect(jsonPath("$.amount").value(123.45))
                        .andExpect(jsonPath("$.createdAt").value(STRING_FIXED_INSTANT))
                        .andExpect(jsonPath("$.updatedAt").value(STRING_FIXED_INSTANT))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        PaymentDto created = objectMapper.readValue(response, PaymentDto.class);

        Optional<Payment> saved = paymentRepository.findById(created.getGuid());
        assertThat(saved).isPresent();
        assertThat(saved.get().getCurrency()).isEqualTo("EUR");
        assertThat(saved.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(123.45));
        assertThat(saved.get().getCreatedAt()).isEqualTo(FIXED_INSTANT);
        assertThat(saved.get().getUpdatedAt()).isEqualTo(FIXED_INSTANT);

    }

    @Test
    void shouldReturnPaymentById() throws Exception {
        UUID existingId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        mockMvc.perform(
                        get("/payments/" + existingId)
                                .with(TestJwtFactory.jwtWithRole("test-user", "reader"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guid").value(existingId.toString()))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(99.99));
    }

    @Test
    void shouldReturn404ForNonExistentPayment() throws Exception {
        // given
        UUID noneExistendId = UUID.randomUUID();

        mockMvc.perform(get("/payments/" + noneExistendId)
                        .with(TestJwtFactory.jwtWithRole("test-user", "reader"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(PAYMENT_NOT_FOUND))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.operation").value("FIND_BY_ID"))
                .andExpect(jsonPath("$.entityGuid").value(noneExistendId.toString()));
    }

    @Test
    void shouldReturnOnlyUSD() throws Exception {
        // given - when

        String searchCurrency = "USD";

        mockMvc.perform(
                        get("/payments/search")
                                .with(TestJwtFactory.jwtWithRole("test-user", "reader"))
                                .param("currency", searchCurrency)
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content[*].currency", everyItem(equalTo(searchCurrency))));
    }

    @Test
    void shouldReturnOnlyNotSentStatus() throws Exception {
        // given - when

        PaymentStatus searchStatus = NOT_SENT;

        mockMvc.perform(
                        get("/payments/search")
                                .with(
                                        TestJwtFactory.jwtWithRole("test-user", "reader")
                                )
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "createdAt")
                                .param("status", searchStatus.toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldDeleteByGuidAndGetNotFound() throws Exception {
        // given - when

        String guid = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(
                        delete("/payments/" + guid)
                                .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(PAYMENT_NOT_FOUND))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.operation").value("DELETE_ENTITY"))
                .andExpect(jsonPath("$.entityGuid").value(guid));
    }

    @Test
    void shouldUpdateStatusThenGet404() throws Exception {
        // given - when

        String guid = "00000000-0000-0000-0000-000000000000";
        PaymentStatusDto dto = new PaymentStatusDto(NOT_SENT);

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(
                        patch("/payments/" + guid + "/status")
                                .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                // then
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateStatus() throws Exception {
        // given - when

        String guid = "00000000-0000-0000-0000-000000000001";
        PaymentStatusDto dto = new PaymentStatusDto(NOT_SENT);

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(
                        patch("/payments/" + guid + "/status")
                                .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                // then
                .andExpect(status().isOk());

        Optional<Payment> p = paymentRepository.findById(UUID.fromString(guid));
        assertThat(p).isPresent();
        assertThat(p.get().getStatus()).isEqualTo(NOT_SENT);
    }

    @Test
    void shouldUpdateNote() throws Exception {
        // given - when

        String guid = "00000000-0000-0000-0000-000000000001";
        PaymentNoteDto dto = new PaymentNoteDto("new note");

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(
                        patch("/payments/" + guid + "/note")
                                .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                // then
                .andExpect(status().isOk());

        Optional<Payment> p = paymentRepository.findById(UUID.fromString(guid));
        assertThat(p).isPresent();
        assertThat(p.get().getNote()).isEqualTo(dto.getNote());
    }

    @Test
    void shouldDeleteByGuid() throws Exception {
        // given - when
        String guid = "00000000-0000-0000-0000-000000000004";
        Optional<Payment> p = paymentRepository.findById(UUID.fromString(guid));
        assertThat(p).isNotEmpty();

        mockMvc.perform(
                        delete("/payments/" + guid)
                                .with(TestJwtFactory.jwtWithRole("test-user", "admin"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                // then
                .andExpect(status().isOk());

        p = paymentRepository.findById(UUID.fromString(guid));
        assertThat(p).isEmpty();
    }
}