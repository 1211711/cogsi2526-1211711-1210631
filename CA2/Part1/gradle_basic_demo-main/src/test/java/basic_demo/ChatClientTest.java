package basic_demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatClientTest {

    @Test
    public void test(){
        ChatClient chatClient = new ChatClient("127.0.0.1", 8080);

        assertNotNull(chatClient);
    }
}
