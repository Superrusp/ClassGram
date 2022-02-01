package com.classgram.backend.repo;

import com.classgram.backend.model.CourseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import com.classgram.backend.model.Course;

public interface CourseDetailsRepository extends JpaRepository<CourseDetails, Long> {
	
	CourseDetails findByCourse(Course course);
	CourseDetails findById(long id);
}
