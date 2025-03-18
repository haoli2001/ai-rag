package cn.quantumforge.api;

import cn.quantumforge.api.dto.resp.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author hao
 * @date 2025/3/17
 **/


public interface IRAGService {
    Response<List<String>> queryRagTagList();

    Response<String> uploadFile(String ragTag, List<MultipartFile> files);

}
