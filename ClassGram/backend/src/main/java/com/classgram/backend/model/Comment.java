package com.classgram.backend.model;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String audioUrl;

	private String message;
	
	private long date;
	
	@OneToMany(mappedBy="commentParent", cascade=CascadeType.ALL)
	@JsonManagedReference
	private List<Comment> replies = new ArrayList<>();
	
	@ManyToOne
	@JsonBackReference
	private Comment commentParent;
	
	@ManyToOne
	private User user;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "comment_telegram_messages",
            joinColumns =
                    {@JoinColumn(name = "comment_id", referencedColumnName = "id")},
            inverseJoinColumns =
                    {@JoinColumn(name = "telegram_message_id", referencedColumnName = "id")})
    private TelegramThreadMessage telegramThreadMessage;

	public Comment(String message, long date, User user) {
		this.message = message;
		this.date = date;
		this.user = user;
		this.commentParent = null;
	}
	
	public Comment(String message, long date, User user, Comment commentParent) {
		this.message = message;
		this.date = date;
		this.user = user;
		this.commentParent = commentParent;
	}


}
