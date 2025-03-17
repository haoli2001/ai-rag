package cn.quantumforge.test;

import cn.quantumforge.app.Application;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hao
 * @date 2025/3/17
 **/

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class RAGTest {
    @Resource
    private OllamaChatClient ollamaChatClient;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private SimpleVectorStore simpleVectorStore;
    @Resource
    private PgVectorStore pgVectorStore;


    @Test
    public void upload() {
        try {
            TikaDocumentReader reader = new TikaDocumentReader("./data/file.text");

            List<Document> documents = reader.get();
            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

            // 打标
            documents.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));
            documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));

            pgVectorStore.accept(documentSplitterList);

            log.info("上传完成");
        } catch (Exception e) {
            log.error("上传过程中出现错误: ", e);
        }
    }

    @Test
    public void chat() {
        String message = "往事苏打，哪年出生";

        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;

        SearchRequest request = SearchRequest.query(message).withTopK(5).withFilterExpression("knowledge == '知识库名称'");

        List<Document> documents = pgVectorStore.similaritySearch(request);
        String documentsCollectors = documents.stream().map(Document::getContent).collect(Collectors.joining());

        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentsCollectors));

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(message));
        messages.add(ragMessage);

        ChatResponse chatResponse = ollamaChatClient.call(new Prompt(messages, OllamaOptions.create().withModel("gemma3:27b")));

        log.info("测试结果:{}", JSON.toJSONString(chatResponse));

    }

    @Test
    public void Localchat() {
        TikaDocumentReader reader = new TikaDocumentReader("./data/file.text");

        List<Document> documents = reader.get();
        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

        // 打标
        documents.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));
        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));
        simpleVectorStore.add(documentSplitterList);

        // 查询
        String message = "往事苏打，哪年出生";

        String SYSTEM_PROMPT = """
            Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
            If unsure, simply state that you don't know.
            Another thing you need to note is that your reply must be in Chinese!
            DOCUMENTS:
                {documents}
            """;

        SearchRequest request = SearchRequest.query(message).withTopK(5); // 移除过滤条件

        List<Document> searchedDocuments = simpleVectorStore.similaritySearch(request);

        // 手动过滤元数据
        List<Document> filteredDocuments = searchedDocuments.stream()
                .filter(doc -> "知识库名称".equals(doc.getMetadata().get("knowledge")))
                .collect(Collectors.toList());

        String documentsCollectors = filteredDocuments.stream().map(Document::getContent).collect(Collectors.joining());

        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentsCollectors));

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(message));
        messages.add(ragMessage);

        ChatResponse chatResponse = ollamaChatClient.call(new Prompt(messages, OllamaOptions.create().withModel("gemma3:27b")));

        log.info("测试结果:{}", JSON.toJSONString(chatResponse));
    }

}
