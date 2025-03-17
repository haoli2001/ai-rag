package cn.quantumforge.app;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author hao
 * @date 2025/3/17
 **/

@SpringBootApplication
@ComponentScan(basePackages = {"cn.quantumforge.app", "cn.quantumforge.trigger","cn.quantumforge.api"})
@Configurable
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
