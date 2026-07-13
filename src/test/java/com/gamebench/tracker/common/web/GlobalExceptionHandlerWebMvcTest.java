package com.gamebench.tracker.common.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ValidationProbeController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void returnsSuccessForValidRequestBody() throws Exception {
        MvcResult result = mockMvc.perform(post("/test/validation/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ValidationProbeController.ValidationBody("benchmark", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("benchmark"))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();

        assertIsoTimestamp(result);
    }

    @Test
    void convertsInvalidRequestBody() throws Exception {
        MvcResult result = mockMvc.perform(post("/test/validation/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ValidationProbeController.ValidationBody("", 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.details.fieldErrors[0].field").value("count"))
                .andExpect(jsonPath("$.error.details.fieldErrors[1].field").value("name"))
                .andExpect(jsonPath("$.error.details.rejectedValue").doesNotExist())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();

        assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException());
        assertIsoTimestamp(result);
    }

    @Test
    void convertsUnreadableJsonWithoutLeakingDetails() throws Exception {
        MvcResult result = mockMvc.perform(post("/test/validation/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"benchmark\",\"count\":\"not-a-number\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.details").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();

        assertInstanceOf(HttpMessageNotReadableException.class, result.getResolvedException());
        assertResponseDoesNotContain(result, "not-a-number");
        assertIsoTimestamp(result);
    }

    @Test
    void convertsInvalidQueryParameter() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/validation/query").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.details.violations[0].path").value("limit"))
                .andExpect(jsonPath("$.error.details.violations[0].invalidValue").doesNotExist())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();

        assertInstanceOf(HandlerMethodValidationException.class, result.getResolvedException());
        assertIsoTimestamp(result);
    }

    @Test
    void convertsInvalidPathParameter() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/validation/path/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.details.violations[0].path").value("id"))
                .andExpect(jsonPath("$.error.details.violations[0].invalidValue").doesNotExist())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();

        assertInstanceOf(HandlerMethodValidationException.class, result.getResolvedException());
        assertIsoTimestamp(result);
    }

    @Test
    void convertsMissingQueryParameter() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/validation/query"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.details").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();

        assertInstanceOf(MissingServletRequestParameterException.class, result.getResolvedException());
        assertIsoTimestamp(result);
    }

    @Test
    void convertsQueryParameterTypeMismatch() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/validation/query").param("limit", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.details").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();

        assertInstanceOf(MethodArgumentTypeMismatchException.class, result.getResolvedException());
        assertResponseDoesNotContain(result, "NumberFormatException");
        assertIsoTimestamp(result);
    }

    @Test
    void convertsApplicationException() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/errors/application"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.details.resource").value("test"))
                .andExpect(jsonPath("$.error.details.cause").doesNotExist())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();

        assertIsoTimestamp(result);
    }

    @Test
    void convertsUnexpectedExceptionWithoutLeakingInternals() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.message").isNotEmpty())
                .andExpect(jsonPath("$.error.details").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();

        assertResponseDoesNotContain(result, "internal-secret-message");
        assertResponseDoesNotContain(result, "IllegalStateException");
        assertIsoTimestamp(result);
    }

    private void assertIsoTimestamp(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        String timestamp = response.path("timestamp").asText();

        assertNotNull(timestamp);
        assertTrue(!timestamp.isBlank());
        Instant.parse(timestamp);
    }

    private void assertResponseDoesNotContain(MvcResult result, String value) throws Exception {
        assertTrue(!result.getResponse().getContentAsString().contains(value));
    }
}