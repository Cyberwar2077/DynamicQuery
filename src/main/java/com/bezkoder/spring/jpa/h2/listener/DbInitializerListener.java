package com.bezkoder.spring.jpa.h2.listener;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.bezkoder.spring.jpa.h2.repository.DynamicRepository;

@Component
public class DbInitializerListener implements ApplicationListener<ApplicationStartedEvent> {

	private DynamicRepository repo;

	DbInitializerListener(DynamicRepository repo) {
		this.repo=repo;
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		repo.initDataBase();
	}
}
