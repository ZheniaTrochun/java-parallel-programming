package com.yevhenii.service;

import com.yevhenii.service.configs.AppPropertyHolder;
import com.yevhenii.service.controllers.SequentialControllerImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest//(classes = Application.class)
@WebMvcTest(SequentialControllerImpl.class)
@ContextConfiguration(classes = Application.class)
public class ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void contextLoads() {
    }

    @Test
    public void sequentialBenchmark() throws Exception {
        mockMvc.perform(get("/admin/clear-bucket"));

        long start = System.currentTimeMillis();

        mockMvc.perform(get("/data/upload/sequential"))
                .andExpect(status().isOk());

        long end = System.currentTimeMillis();
        System.out.println("Squential upload took: " + (end - start) + "ms");
    }
}
