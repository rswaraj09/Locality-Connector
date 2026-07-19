package com.example.localityconnector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class Item {
	@Id
	private String id;

	@NotBlank
	@Indexed
	private String businessId;

	@NotBlank
	private String businessName;

	@NotBlank
	private String name;

	@NotNull
	private Double price;

	private String description;

	private String category;

	private boolean available = true;

	private Integer stock = 100;

	// Item image URL
	private String imageUrl;

	private List<String> imageUrls = new ArrayList<>();

	private Date createdAt;

	private Date updatedAt;

	public void prePersist() {
		if (createdAt == null) {
			createdAt = new Date();
		}
		updatedAt = new Date();
	}
}
