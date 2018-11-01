package com.yevhenii.service.controllers;

import com.yevhenii.service.services.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdministrationControllerImpl implements AdministrationController {

    private final AdministrationService service;

    @Autowired
    public AdministrationControllerImpl(AdministrationService service) {
        this.service = service;
    }

    @Override
    @RequestMapping("/admin/clear-bucket")
    public ResponseEntity<Void> deleteAllRecords() {
        return service.deleteAll() ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }

    @Override
    @RequestMapping("/admin/bucket-size")
    public ResponseEntity<Integer> getDbSize() {
        return ResponseEntity.ok(service.getDbSize());
    }
}
