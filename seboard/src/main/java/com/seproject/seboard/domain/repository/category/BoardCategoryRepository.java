package com.seproject.seboard.domain.repository.category;

import com.seproject.seboard.domain.model.category.BoardCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {
}
