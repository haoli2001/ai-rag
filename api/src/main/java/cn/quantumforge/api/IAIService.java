package cn.quantumforge.api;

import org.springframework.ai.chat.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * @author hao
 * @date 2025/3/17
 **/

public interface IAIService {
    ChatResponse generate(String model , String message);


    Flux<ChatResponse>  generateStream(String model , String message);

}
