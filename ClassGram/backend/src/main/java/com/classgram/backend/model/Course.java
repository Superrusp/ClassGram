package com.classgram.backend.model;

import java.util.Set;
import java.util.HashSet;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Course {

	public interface SimpleCourseList {}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonView(SimpleCourseList.class)
	private long id;

	@JsonView(SimpleCourseList.class)
	private String title;

	@JsonView(SimpleCourseList.class)
	private String image;

	@ManyToOne
	private User teacher;

	@OneToOne(cascade=CascadeType.ALL)
	private CourseDetails courseDetails;

	@JsonView(SimpleCourseList.class)
	@OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="course")
	private Set<Session> sessions = new HashSet<>();

	@ManyToMany
	private Set<User> attenders = new HashSet<>();

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "course_telegram_chats",
            joinColumns =
                    {@JoinColumn(name = "course_id", referencedColumnName = "id")},
            inverseJoinColumns =
                    {@JoinColumn(name = "telegram_channel_id", referencedColumnName = "id")})
    private TelegramChannel telegramChannel;

	public Course(String title, String image, User teacher) {
		this.title = title;
		this.image = image;
		this.teacher = teacher;
		this.courseDetails = null;
	}

	public Course(String title, String image, User teacher, CourseDetails courseDetails) {
		this.title = title;
		this.image = image;
		this.teacher = teacher;
		this.courseDetails = courseDetails;
	}
}
