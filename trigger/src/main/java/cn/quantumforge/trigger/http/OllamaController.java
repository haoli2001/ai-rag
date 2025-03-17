package cn.quantumforge.trigger.http;

import cn.quantumforge.api.IAIService;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author hao
 * @date 2025/3/17
 **/


@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/ollama/")
public class OllamaController implements IAIService {

    @Autowired
    private OllamaChatClient ollamaChatClient;

    /**
     * http://127.0.0.1:8090/api/v1/ollama/generate?model=gemma3:27b&message=你好
     *
     * @param model
     * @param message
     * @return
     */
    @RequestMapping(value = "generate", method = RequestMethod.GET)
    @Override
    public ChatResponse generate(@RequestParam(name = "model") String model, @RequestParam(name = "message") String message) {
        return ollamaChatClient.call(new Prompt(message, OllamaOptions.create().withModel(model)));
    }

    /**
     * 生成流式数据
     * http://127.0.0.1:8090/api/v1/ollama/generate_stream?model=gemma3:27b&message=你好
     *
     * @param model
     * @param message
     * @return
     */
    @RequestMapping(value = "generate_stream", method = RequestMethod.GET)
    @Override
    public Flux<ChatResponse> generateStream(@RequestParam(name = "model") String model, @RequestParam(name = "message") String message) {
        return ollamaChatClient.stream(new Prompt(message, OllamaOptions.create().withModel(model)));
    }
}
