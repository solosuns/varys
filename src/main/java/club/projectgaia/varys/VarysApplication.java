package club.projectgaia.varys;

import club.projectgaia.varys.service.SpriderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class VarysApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VarysApplication.class);

    @Autowired
    SpriderHandler handler;

    public static void main(String[] args) {
        SpringApplication.run(VarysApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        handler.getType();
    }
}