package com.bezkoder.spring.jpa.h2.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.bezkoder.spring.jpa.h2.model.Student;
import com.bezkoder.spring.jpa.h2.model.Tutorial;
import com.bezkoder.spring.jpa.h2.util.Helper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

@Repository
public class DynamicRepository {

	private final Logger LOGGER = LoggerFactory.getLogger(DynamicRepository.class);
	private EntityManager entityManager;

	DynamicRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	// adding dummy data into db
	@Transactional
	public void initDataBase() {
		entityManager.createQuery("Delete from Student").executeUpdate();
		entityManager.createQuery("Delete from Tutorial").executeUpdate();
		Student s1 = getStudent("s1", 1);
		Student s2 = getStudent("s2", 2);
		Student s3 = getStudent("s3", 3);
		Tutorial t1 = getTutorial("test1", "sample1");
		Tutorial t2 = getTutorial("test2", "sample2");
		saveAll(Arrays.asList(t1, t2));
		s1.setTutorials(Arrays.asList(t1, t2));
		s2.setTutorials(Arrays.asList(t1));
		s1.setFirstEnrolled(t1);
		s2.setFirstEnrolled(t2);
		saveAll(Arrays.asList(s1, s2, s3));
	}

	private Tutorial getTutorial(String desc, String title) {
		Tutorial t1 = new Tutorial();
		t1.setDescription(desc);
		t1.setTitle(title);
		return t1;
	}

	private void saveAll(List<Object> objs) {
		for (Object obj : objs) {
			entityManager.persist(obj);
		}
	}

	private Student getStudent(String name, int id) {
		Student s1 = new Student();
		s1.setName(name);
		s1.setRollNum(id);
		return s1;
	}

	public List<Student> fetchStudentsByNameWithRequiredFields(List<String> studentNames, Collection<String> values) {
		if (!values.isEmpty()) {
			try {
				Gson gson = Helper.getGson();
				// whitelisting values to allow only alphabets without spaces in names
				values = values.stream().filter(value -> value.matches("[a-zA-Z\\.0-9]+")).map(String::trim).toList();
				List<Object[]> data = fetchRequiredFieldsFromDb(studentNames, values);
				List<Map> list = new ArrayList<>();
				for (Object[] row : data) {
					list.add(Helper.convertRowToMap(row, values));
				}
				LOGGER.info("partial map is " + list);
				JsonElement jsonElement = gson.toJsonTree(list);
				Class<Student[]> arrayClass = null;
				arrayClass = (Class<Student[]>) Class.forName("[L" + Student.class.getName() + ";");
				Student[] convertedArrayRows = gson.fromJson(jsonElement, arrayClass);
				List<Student> studentList = Arrays.asList(convertedArrayRows);
				return studentList;
			} catch (Exception e) {
				LOGGER.error("error while coverting to object", e);
			}
		} else {
			TypedQuery<Student> createQuery = entityManager.createQuery("from Student where name in (:studentNames)",
					Student.class);
			createQuery.setParameter("studentNames", studentNames);
			return createQuery.getResultList();
		}
		return Collections.EMPTY_LIST;
	}

	public List<Object[]> fetchRequiredFieldsFromDb(List<String> studentNames, Collection<String> values) {
		LOGGER.info("Started Execution");
		long tempTime = System.currentTimeMillis();
		String hqlQuery = "select " + String.join(",", values) + " from Student where name in (:studentNames)";
		LOGGER.info("query is " + hqlQuery);
		TypedQuery<Object[]> createQuery = entityManager.createQuery(hqlQuery, Object[].class);
		createQuery.setParameter("studentNames", studentNames);
		List<Object[]> dbValues = null;
		try {
			dbValues = createQuery.getResultList();
			LOGGER.info("fetchRequiredFieldsFromDb() :: Time taken to get student is "
					+ (System.currentTimeMillis() - tempTime) + " ms to fetch " + dbValues.size());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		LOGGER.info("End Execution");
		return dbValues;
	}
}
