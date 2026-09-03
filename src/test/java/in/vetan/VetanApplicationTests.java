package in.vetan;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class VetanApplicationTests {

	@Test
	void contextLoads() {
	}

    @Test
    void deliberateFailureToProveTheGate() {
        fail("deliberate failure - proving CI blocks the merge");
    }

}
