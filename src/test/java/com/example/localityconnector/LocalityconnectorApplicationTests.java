package com.example.localityconnector;

import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class LocalityconnectorApplicationTests {

	@MockBean(name = "firestore")
	Firestore firestore;

	@MockBean
	org.springframework.mail.javamail.JavaMailSender javaMailSender;

	@MockBean(name = "firebaseStorage")
	com.google.cloud.storage.Storage firebaseStorage;

	@Test
	void contextLoads() {
	}

}
