package server.game.pushing.paper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import server.game.pushing.paper.level.Level;
import server.game.pushing.paper.level.LevelController;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LevelController.class)
public class LevelTests {
    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;

    @Test
    protected void the_client_can_get_a_level_from_the_server() throws Exception {
        RequestBuilder requestBuilder = get("/");

        System.out.println(mockMvc.perform(requestBuilder).andReturn().getResponse().getContentAsString());
/*
        assertNotNull(level.order);
        assertNotNull(level.receipt);
        assertNotNull(level.transformation);
*/
        mockMvc.perform(requestBuilder).andExpect(status().isOk());
    }
}
