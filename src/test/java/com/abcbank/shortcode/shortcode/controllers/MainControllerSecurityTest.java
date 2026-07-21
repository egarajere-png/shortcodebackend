// package com.abcbank.shortcode.shortcode.controllers;

// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// import java.io.ByteArrayInputStream;
// import java.util.List;

// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Import;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.beans.factory.annotation.Autowired;

// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

// import com.abcbank.shortcode.shortcode.config.SecurityConfig;
// import com.abcbank.shortcode.shortcode.middleware.AuditTrailService;
// import com.abcbank.shortcode.shortcode.middleware.FinacleData;
// import com.abcbank.shortcode.shortcode.middleware.ShortCodeService;
// import com.abcbank.shortcode.shortcode.repo.AuditTrailRepo;
// import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;
// import com.abcbank.shortcode.shortcode.services.ExportService;
// import com.abcbank.shortcode.shortcode.utils.ShortCodeMapper;

// /**
//  * ==========================================================
//  * MainController Security Tests
//  * ==========================================================
//  *
//  * Tests authentication and authorization behaviour.
//  *
//  * Verifies:
//  *
//  * - Unauthenticated requests
//  * - Maker permissions
//  * - Checker permissions
//  * - API Caller permissions
//  * - Forbidden role access
//  * - Public endpoint accessibility
//  *
//  * Business logic is intentionally NOT tested here.
//  */
// @SpringBootTest
// @AutoConfigureMockMvc
// class MainControllerSecurityTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private ShortCodeRepo shortCodeRepo;

//     @MockBean
//     private AuditTrailRepo auditTrailRepo;

//     @MockBean
//     private ShortCodeMapper shortCodeMapper;

//     @MockBean
//     private FinacleData finacleData;

//     @MockBean
//     private ShortCodeService shortCodeService;

//     @MockBean
//     private AuditTrailService auditTrailService;

//     @MockBean
//     private UtilController utilController;

//     @MockBean
//     private ExportService exportService;

//     /*
//      * ---------------------------------------------------------
//      * Authentication Tests
//      * ---------------------------------------------------------
//      */

//     @Test
//     @DisplayName("Should reject unauthenticated request")
//     void shouldRejectUnauthenticatedRequest() throws Exception {

//         mockMvc.perform(get("/shortcodes/api/pending"))
//                 .andExpect(status().isUnauthorized());
//     }

//     /*
//      * ---------------------------------------------------------
//      * Maker Role Tests
//      * ---------------------------------------------------------
//      */

//     @Test
//     @DisplayName("Maker should access initiate endpoint")
//     void makerShouldAccessInitiate() throws Exception {

//         mockMvc.perform(post("/shortcodes/api/initiate")
//                         .with(jwt().authorities(() -> "ROLE_maker"))
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("{}"))
//                 .andExpect(status().isOk());
//     }

//     @Test
//     @DisplayName("Maker should access delete endpoint")
//     void makerShouldAccessDelete() throws Exception {

//         mockMvc.perform(delete("/shortcodes/api/delete")
//                         .with(jwt().authorities(() -> "ROLE_maker"))
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("{}"))
//                 .andExpect(status().isOk());
//     }

//     @Test
//     @DisplayName("Maker should NOT access approve endpoint")
//     void makerShouldNotApprove() throws Exception {

//         mockMvc.perform(post("/shortcodes/api/approve")
//                         .with(jwt().authorities(() -> "ROLE_maker"))
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("{}"))
//                 .andExpect(status().isForbidden());
//     }

//     /*
//      * ---------------------------------------------------------
//      * Checker Role Tests
//      * ---------------------------------------------------------
//      */

//     @Test
//     @DisplayName("Checker should approve requests")
//     void checkerShouldApprove() throws Exception {

//         mockMvc.perform(post("/shortcodes/api/approve")
//                         .with(jwt().authorities(() -> "ROLE_checker"))
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("{}"))
//                 .andExpect(status().isOk());
//     }

//     @Test
//     @DisplayName("Checker should NOT initiate requests")
//     void checkerShouldNotInitiate() throws Exception {

//         mockMvc.perform(post("/shortcodes/api/initiate")
//                         .with(jwt().authorities(() -> "ROLE_checker"))
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("{}"))
//                 .andExpect(status().isForbidden());
//     }

//     /*
//      * ---------------------------------------------------------
//      * API Caller Role Tests
//      * ---------------------------------------------------------
//      */

//     @Test
//     @DisplayName("API Caller should access validation endpoint")
//     void apiCallerShouldValidate() throws Exception {

//         mockMvc.perform(get("/shortcodes/api/validate/123456789")
//                         .with(jwt().authorities(() -> "ROLE_apicaller")))
//                 .andExpect(status().isOk());
//     }

//     @Test
//     @DisplayName("API Caller should approve requests")
//     void apiCallerShouldApprove() throws Exception {

//         mockMvc.perform(post("/shortcodes/api/approve")
//                         .with(jwt().authorities(() -> "ROLE_apicaller"))
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content("{}"))
//                 .andExpect(status().isOk());
//     }

//     /*
//      * ---------------------------------------------------------
//      * Read Endpoint Tests
//      * ---------------------------------------------------------
//      */

//     @Test
//     @DisplayName("Authenticated user should access pending endpoint")
//     void authenticatedUserShouldAccessPending() throws Exception {

//         mockMvc.perform(get("/shortcodes/api/pending")
//                         .with(jwt().authorities(() -> "ROLE_checker")))
//                 .andExpect(status().isOk());
//     }

//     @Test
//     @DisplayName("Authenticated user should access registry")
//     void authenticatedUserShouldAccessRegistry() throws Exception {

//         mockMvc.perform(get("/shortcodes/api/registry")
//                         .with(jwt().authorities(() -> "ROLE_apicaller")))
//                 .andExpect(status().isOk());
//     }
// }