package com.example.localityconnector.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Cursor-based pagination envelope returned by repositories that paginate natively
 * in Firestore.
 *
 * @param <T> the item type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResult<T> {
    private List<T> items;
    /** Document id to pass as {@code startAfter} for the next page; null when there is no next page. */
    private String nextCursor;
    private boolean hasMore;
}
