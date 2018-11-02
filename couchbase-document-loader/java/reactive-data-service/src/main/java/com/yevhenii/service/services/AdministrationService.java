package com.yevhenii.service.services;

public interface AdministrationService {

    boolean deleteAll();

    boolean recreateBucket();

    int getDbSize();
}
