package com.yevhenii.service.services;

import com.yevhenii.service.dao.CouchbaseDao;
import com.yevhenii.service.models.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdministrationServiceImpl implements AdministrationService {

    private final CouchbaseDao<DataObject> dao;

    @Autowired
    public AdministrationServiceImpl(CouchbaseDao<DataObject> dao) {
        this.dao = dao;
    }

    @Override
    public boolean deleteAll() {
        return dao.deleteAll();
    }

    @Override
    public boolean recreateBucket() {
        return dao.recreateBucket();
    }

    @Override
    public int getDbSize() {
        return dao.getSize();
    }
}
