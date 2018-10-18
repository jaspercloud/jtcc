package com.jaspercloud.tcc.client.support.persistence;

import java.util.Map;

public interface TccMethodDataDao {

    void saveData(Map<String, String> map);

    Map<String, String> getData();

    void deleteData();
}
