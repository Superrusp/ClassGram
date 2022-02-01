package com.classgram.backend.entry;

import com.classgram.backend.model.Comment;
import com.classgram.backend.model.Entry;

public class NewEntryCommentResponse {
	
	Entry entry;
	Comment comment;
	
	public NewEntryCommentResponse(Entry entry, Comment comment) {
		this.entry = entry;
		this.comment = comment;
	}
	
	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	
	public Comment getComment() {
		return comment;
	}
	
	public void setComment(Comment comment) {
		this.comment = comment;
	}
	
}
