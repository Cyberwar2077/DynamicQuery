package com.bezkoder.spring.jpa.h2.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="student_id")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "roll_num")
	private Integer rollNum;

	@ManyToMany
	@JoinTable(
			  name = "student_tutorial", 
			  joinColumns = @JoinColumn(name = "student_id"), 
			  inverseJoinColumns = @JoinColumn(name = "id"))
	private List<Tutorial> tutorials;
	
	@ManyToOne
	private Tutorial firstEnrolled;
	
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRollNum() {
		return rollNum;
	}

	public void setRollNum(Integer rollNum) {
		this.rollNum = rollNum;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<Tutorial> getTutorials() {
		return tutorials;
	}

	public void setTutorials(List<Tutorial> tutorials) {
		this.tutorials = tutorials;
	}

	public Tutorial getFirstEnrolled() {
		return firstEnrolled;
	}

	public void setFirstEnrolled(Tutorial firstEnrolled) {
		this.firstEnrolled = firstEnrolled;
	}

}
