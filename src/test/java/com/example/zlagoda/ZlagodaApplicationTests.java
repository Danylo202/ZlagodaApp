package com.example.zlagoda;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:hsqldb:mem:zlagoda-context-test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.sql.init.mode=never"
})
class ZlagodaApplicationTests {

	@Test
	void contextLoads() {
	}

}
