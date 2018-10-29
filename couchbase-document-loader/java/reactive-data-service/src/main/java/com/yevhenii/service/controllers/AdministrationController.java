package com.yevhenii.service.controllers;

import org.springframework.http.ResponseEntity;

public interface AdministrationController {

    ResponseEntity<Void> deleteAllRecords();

    ResponseEntity<Integer> getDbSize();
}
