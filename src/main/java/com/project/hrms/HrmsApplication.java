package com.project.hrms;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class HrmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrmsApplication.class, args);
	}
    @PostConstruct
    public void init() {
        // Thiết lập Timezone mặc định cho toàn bộ ứng dụng là Việt Nam (UTC+7)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        System.out.println("✅ Application running in timezone: " + TimeZone.getDefault().getID());
    }
}
