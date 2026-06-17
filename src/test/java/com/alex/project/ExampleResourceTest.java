package com.alex.project;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Disabled("No /hello endpoint exists in this service")
class ExampleResourceTest {
    @Test
    void testHelloEndpoint() {
    }
}
