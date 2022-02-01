package com.classgram.backend.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Entry {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	private String title;
	
	private long date;
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true)
	private List<Comment> comments = new ArrayList<>();

	@JsonIgnoreProperties("entries")
	@ManyToOne
	private Forum forum;

	@ManyToOne
	private User user;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "entry_telegram_threads",
            joinColumns =
                    {@JoinColumn(name = "entry_id", referencedColumnName = "id")},
            inverseJoinColumns =
                    {@JoinColumn(name = "telegram_thread_id", referencedColumnName = "id")})
    private TelegramThreadChannel telegramThreadChannel;

	public Entry(String title, long date, User user) {
		this.title = title;
		this.date = date;
		this.user = user;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Entry entry = (Entry) o;
		return id == entry.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
