package com.inn.cafe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.utils.EmailUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end API checks against an in-memory database (seeded from dev-catalogue.sql:
 * product 1 = Plain Dosa Rs 49, 2 = Masala Dosa Rs 59, 3 = Cheese Dosa Rs 79).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserDao userDao;
    @Autowired JwtUtil jwtUtil;
    @MockBean EmailUtils emailUtils;

    private static final String ADMIN = "admin@cafeease.test";
    private static final String ADMIN_PASSWORD = "admin-password-123";

    // ---------- helpers ----------

    private String newUser() throws Exception {
        String email = "u" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        mvc.perform(post("/user/signup").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                "name", "Test User", "email", email, "contactNumber", "9999999999", "password", "password123"))))
                .andExpect(status().isOk());
        return email;
    }

    private String login(String email, String password) throws Exception {
        JsonNode body = read(mvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()));
        return body.path("data").path("token").asText();
    }

    private String token(String email) throws Exception {
        return login(email, "password123");
    }

    private MockHttpServletRequestBuilder as(String token, MockHttpServletRequestBuilder req) {
        return req.header("Authorization", "Bearer " + token);
    }

    private JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private JsonNode putCart(String token, Object items) throws Exception {
        return read(mvc.perform(as(token, put("/cart")).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(items))).andExpect(status().isOk())).path("data");
    }

    private JsonNode placeOrder(String token) throws Exception {
        putCart(token, List.of(Map.of("productId", 1, "quantity", 2), Map.of("productId", 2, "quantity", 1)));
        return read(mvc.perform(as(token, post("/orders"))).andExpect(status().isOk())).path("data");
    }

    private ResultActions setStatus(String token, int orderId, String status) throws Exception {
        return mvc.perform(as(token, post("/orders/" + orderId + "/status")).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("status", status))));
    }

    // ---------- auth & passwords ----------

    @Test
    void signupStoresBcryptHashAndLoginReturnsJsonToken() throws Exception {
        String email = newUser();
        assertThat(userDao.findByEmailId(email).getPassword()).startsWith("{bcrypt}");
        assertThat(token(email)).isNotBlank();
    }

    @Test
    void legacyPlaintextPasswordStillLogsInAndIsUpgraded() throws Exception {
        User legacy = new User();
        legacy.setName("Legacy");
        legacy.setEmail("legacy@test.com");
        legacy.setContactNumber("1");
        legacy.setPassword("oldplain1");
        legacy.setStatus("true");
        legacy.setRole("user");
        userDao.save(legacy);

        assertThat(login("legacy@test.com", "oldplain1")).isNotBlank();
        assertThat(userDao.findByEmailId("legacy@test.com").getPassword()).startsWith("{bcrypt}");
        assertThat(login("legacy@test.com", "oldplain1")).isNotBlank(); // still works after upgrade
    }

    @Test
    void roleClaimInTokenGrantsNothing() throws Exception {
        String email = newUser();
        String forged = jwtUtil.generateToken(email, "admin"); // validly signed, lying role claim
        mvc.perform(as(forged, get("/user/get"))).andExpect(status().isForbidden());
    }

    @Test
    void badTokenDoesNotBreakPublicEndpoints() throws Exception {
        mvc.perform(as("not-a-jwt", get("/product"))).andExpect(status().isOk());
        mvc.perform(as("not-a-jwt", get("/cart"))).andExpect(status().isUnauthorized());
    }

    @Test
    void disabledAccountGetsSpecificMessageAndItsTokenStopsWorking() throws Exception {
        String email = newUser();
        String userToken = token(email);
        String admin = login(ADMIN, ADMIN_PASSWORD);
        Integer id = userDao.findByEmailId(email).getId();
        mvc.perform(as(admin, post("/user/update")).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("id", id.toString(), "status", "false"))))
                .andExpect(status().isOk());

        JsonNode body = read(mvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", "password123"))))
                .andExpect(status().isForbidden()));
        assertThat(body.path("message").asText()).contains("disabled");
        mvc.perform(as(userToken, get("/cart"))).andExpect(status().isUnauthorized());
    }

    @Test
    void passwordResetLinkWorksOnce() throws Exception {
        String email = newUser();
        mvc.perform(post("/user/forgotPassword").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email)))).andExpect(status().isOk());
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(emailUtils).sendPasswordReset(eq(email), link.capture());
        String token = link.getValue().substring(link.getValue().indexOf("token=") + 6);

        String reset = json.writeValueAsString(Map.of("token", token, "password", "brand-new-pass"));
        mvc.perform(post("/user/resetPassword").contentType(MediaType.APPLICATION_JSON).content(reset))
                .andExpect(status().isOk());
        assertThat(login(email, "brand-new-pass")).isNotBlank();
        mvc.perform(post("/user/resetPassword").contentType(MediaType.APPLICATION_JSON).content(reset))
                .andExpect(status().isBadRequest());
    }

    // ---------- the shared-user race ----------

    @Test
    void concurrentRequestsNeverSeeAnotherUsersCart() throws Exception {
        String a = newUser(), b = newUser();
        String ta = token(a), tb = token(b);
        putCart(ta, List.of(Map.of("productId", 1, "quantity", 1)));
        putCart(tb, List.of(Map.of("productId", 2, "quantity", 1)));

        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<Boolean>> checks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String email = i % 2 == 0 ? a : b;
            String tok = i % 2 == 0 ? ta : tb;
            checks.add(pool.submit((Callable<Boolean>) () -> email.equals(
                    read(mvc.perform(as(tok, get("/cart")))).path("data").path("customerDetails").path("email").asText())));
        }
        for (Future<Boolean> check : checks) {
            assertThat(check.get()).isTrue();
        }
        pool.shutdown();
    }

    // ---------- cart ----------

    @Test
    void removingALineKeepsTheOtherLinesInTheTotals() throws Exception {
        String t = token(newUser());
        putCart(t, List.of(Map.of("productId", 1, "quantity", 1), Map.of("productId", 2, "quantity", 1),
                Map.of("productId", 3, "quantity", 1)));
        JsonNode cart = putCart(t, List.of(Map.of("productId", 1, "quantity", 0)));
        assertThat(cart.path("items")).hasSize(2);
        assertThat(cart.path("totalQuantity").asInt()).isEqualTo(2);
        assertThat(cart.path("totalAmount").asDouble()).isEqualTo(59 + 79);
    }

    @Test
    void negativeQuantityIsRejectedAndClearEmptiesTheCart() throws Exception {
        String t = token(newUser());
        mvc.perform(as(t, put("/cart")).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(List.of(Map.of("productId", 1, "quantity", -1)))))
                .andExpect(status().isBadRequest());
        putCart(t, List.of(Map.of("productId", 1, "quantity", 2)));
        mvc.perform(as(t, delete("/cart"))).andExpect(status().isOk());
        JsonNode cart = read(mvc.perform(as(t, get("/cart")))).path("data");
        assertThat(cart.path("totalQuantity").asInt()).isZero();
        assertThat(cart.path("items")).isEmpty();
    }

    @Test
    void placingAnOrderWithNoCartIsABadRequest() throws Exception {
        mvc.perform(as(token(newUser()), post("/orders"))).andExpect(status().isBadRequest());
    }

    // ---------- orders ----------

    @Test
    void orderAddsFivePercentTaxAndStartsPlaced() throws Exception {
        JsonNode order = placeOrder(token(newUser()));
        double subtotal = 2 * 49 + 59;
        assertThat(order.path("taxAmount").asDouble()).isEqualTo(Math.round(subtotal * 5) / 100.0);
        assertThat(order.path("totalAmount").asDouble()).isCloseTo(subtotal * 1.05, org.assertj.core.data.Offset.offset(0.01));
        assertThat(order.path("statusCode").asText()).isEqualTo("ORDER_PLACED");
    }

    @Test
    void statusLifecycleIsEnforced() throws Exception {
        String user = token(newUser());
        String admin = login(ADMIN, ADMIN_PASSWORD);
        int id = placeOrder(user).path("orderId").asInt();

        setStatus(user, id, "PREPARING").andExpect(status().isForbidden());
        assertThat(read(setStatus(admin, id, "PREPARING").andExpect(status().isOk()))
                .path("data").path("statusCode").asText()).isEqualTo("PREPARING");
        // customers can't cancel once the kitchen has started
        mvc.perform(as(user, post("/orders/cancel/" + id))).andExpect(status().isBadRequest());
        mvc.perform(as(admin, post("/orders/cancel/" + id))).andExpect(status().isOk());
        setStatus(admin, id, "READY").andExpect(status().isBadRequest()); // cancelled is final
        setStatus(admin, id, "NOT_A_STATUS").andExpect(status().isBadRequest());
    }

    @Test
    void customerCanCancelTheirOwnPlacedOrderAndItIsKept() throws Exception {
        String user = token(newUser());
        int id = placeOrder(user).path("orderId").asInt();
        mvc.perform(as(user, post("/orders/cancel/" + id))).andExpect(status().isOk());
        JsonNode orders = read(mvc.perform(as(user, post("/orders/search")).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("orderId", id))))).path("data");
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).path("statusCode").asText()).isEqualTo("CANCELLED");
    }

    @Test
    void ordersAndBillsOfOtherCustomersAreNotVisible() throws Exception {
        int id = placeOrder(token(newUser())).path("orderId").asInt();
        String other = token(newUser());
        mvc.perform(as(other, get("/bill/" + id))).andExpect(status().isNotFound());
        mvc.perform(as(other, post("/orders/cancel/" + id))).andExpect(status().isNotFound());
        assertThat(read(mvc.perform(as(other, post("/orders/search")).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("orderId", id))))).path("data")).isEmpty();
    }

    @Test
    void adminOnlyEndpointsRejectCustomers() throws Exception {
        String user = token(newUser());
        mvc.perform(as(user, post("/orders/admin")).param("emailId", ADMIN).contentType(MediaType.APPLICATION_JSON)
                .content("[{\"productId\":1,\"quantity\":1}]")).andExpect(status().isForbidden());
        mvc.perform(as(user, post("/product/bulk"))).andExpect(status().isForbidden());
        mvc.perform(as(user, get("/contact"))).andExpect(status().isForbidden());
    }

    // ---------- bills ----------

    @Test
    void billIsAPdfForTheOwnerAndAdminAndCanBeEmailed() throws Exception {
        String user = token(newUser());
        int id = placeOrder(user).path("orderId").asInt();

        byte[] pdf = mvc.perform(as(user, get("/bill/" + id))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
        mvc.perform(as(login(ADMIN, ADMIN_PASSWORD), get("/bill/" + id))).andExpect(status().isOk());

        mvc.perform(as(user, post("/bill/" + id + "/email"))).andExpect(status().isOk());
        verify(emailUtils).sendWithAttachment(anyString(), anyString(), anyString(), eq("CafeEase-Invoice-" + id + ".pdf"), any());
    }

    // ---------- contact ----------

    @Test
    void contactFormValidatesAndAdminCanReadIt() throws Exception {
        mvc.perform(post("/contact").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("name", "Priya", "email", "not-an-email", "message", "Hi"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/contact").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("name", "Priya", "email", "priya@test.com", "message", "Do you cater?"))))
                .andExpect(status().isOk());
        JsonNode inbox = read(mvc.perform(as(login(ADMIN, ADMIN_PASSWORD), get("/contact"))).andExpect(status().isOk()));
        assertThat(inbox.path("data").toString()).contains("Do you cater?");
    }
}
