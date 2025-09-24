package com.example.localityconnector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class Item {
	@Id
	private String id;

	@NotBlank
	private String businessId;

	@NotBlank
	private String businessName;

	@NotBlank
	private String name;

	@NotNull
	private Double price;

	private String description;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		updatedAt = LocalDateTime.now();
	}
}






