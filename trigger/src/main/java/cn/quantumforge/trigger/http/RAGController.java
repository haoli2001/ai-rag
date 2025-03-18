package cn.quantumforge.trigger.http;

import cn.quantumforge.api.IRAGService;
import cn.quantumforge.api.dto.resp.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author hao
 * @date 2025/3/17
 **/


@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/rag/")
public class RAGController implements IRAGService {

    @Resource
    private OllamaChatClient ollamaChatClient;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private SimpleVectorStore simpleVectorStore;
    @Resource
    private PgVectorStore pgVectorStore;
    @Resource
    private RedissonClient redissonClient;


    /**
     * 查询rag标签列表
     * @return
     */
    @RequestMapping(value = "queryRagTagList" , method = RequestMethod.GET)
    @Override
    public Response<List<String>> queryRagTagList() {
        RList<String> ragTag = redissonClient.getList("ragTag");
        return Response.<List<String>>builder()
                .code("200")
                .info("上传成功")
                .data(ragTag)
                .build();
    }





    /**
     *  上传知识库
     * @param ragTag
     * @param files
     * @return
     */
    @RequestMapping(value = "uploadFile" , method = RequestMethod.POST,headers = "content-type=multipart/form-data")
    @Override
    public Response<String> uploadFile(@RequestParam String ragTag, @RequestParam List<MultipartFile> files) {
        log.info("开始上传知识库");
        for(MultipartFile file : files){
            TikaDocumentReader reader = new TikaDocumentReader(file.getResource());
            List<Document> documents = reader.get();
            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

            // 打标
            documents.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
            documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));

            pgVectorStore.accept(documentSplitterList);

            RList<String> elements = redissonClient.getList("ragTag");
            if(!elements.contains(ragTag)){
                elements.add(ragTag);
            }
            log.info("上传完成");

        }
        return Response.<String>builder().code("200").info("上传成功").build();
    }
}
