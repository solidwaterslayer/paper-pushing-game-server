package server.game.pushing.paper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import server.game.pushing.paper.level.Level;
import server.game.pushing.paper.level.LevelController;
import server.game.pushing.paper.store.Store;

import java.util.List;

import static java.lang.Double.parseDouble;
import static java.lang.String.valueOf;
import static java.util.Collections.swap;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static server.game.pushing.paper.level.Mutation.Move;
import static server.game.pushing.paper.level.Mutation.Typo;

@WebMvcTest(LevelController.class)
public class LevelTests {
    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;

    @Test
    protected void the_client_can_get_a_level_from_the_server() throws Exception {
        for (int i = 0; i < 100; i++) {
            RequestBuilder requestBuilder = get("/");
            String level = mockMvc.perform(requestBuilder).andReturn().getResponse().getContentAsString();

            assertTrue(level.contains("order"));
            assertTrue(level.contains("receipt"));
            assertTrue(level.contains("transformation"));
            mockMvc.perform(requestBuilder).andExpect(status().isOk());
        }
    }

    @Test
    protected void a_level_contains_an_order_of_size_6_its_receipt_and_a_transformation() {
        for (int i = 0; i < 100; i++) {
            Level level = new Level();

            assertEquals(6, level.order.size());
            assertNotNull(level.receipt);

            for (int j = 0; j < level.transformation.size(); j++) {
                String potentialMutation = level.transformation.get(j);
                assertTrue(potentialMutation.equals(valueOf(j))
                        || potentialMutation.equals(Typo.name().toLowerCase())
                        || potentialMutation.equals(Move.name().toLowerCase())
                );
            }
        }
    }

    @Test
    protected void a_transformation_has_locations_of_receipt_mutations() {
        for (int i = 0; i < 100; i++) {
            Level level = new Level();
            List<String> order = level.order;
            List<String> actual = level.receipt;
            List<String> transformation = level.transformation;
            List<String> expected = (new Store() {{ setOrder(order); }}).getReceipt();

            for (int j = 0; j < transformation.size(); j++) {
                if (transformation.get(j).equalsIgnoreCase(Move.name())) {
                    swap(actual, j, j - 1);
                    swap(transformation, j, j - 1);
                }
            }
            for (int j = 0; j < transformation.size(); j++) {
                if (transformation.get(j).equalsIgnoreCase(Typo.name())) {
                    String[] actualArguments = actual.get(j).split(" ");
                    String[] expectedArguments = expected.get(j).split(" ");
                    for (int k = 0; k < actualArguments.length; k++) {
                        String actualArgument = actualArguments[k];
                        String expectedArgument = expectedArguments[k];

                        if (!actualArgument.equals(expectedArgument)) {
                            assertTrue(
                                    actualArgument.length() + 1 == expectedArgument.length()
                                            || parseDouble(actualArgument) - 1 == parseDouble(expectedArgument)
                                            || parseDouble(actualArgument) - 100 == parseDouble(expectedArgument)
                            );
                            actual.set(j, expected.get(j));
                        }
                    }
                }
            }
            assertEquals(actual, expected);
        }
    }
}
