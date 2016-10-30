package com.company.navigationeditor.web.customer;

import com.company.navigationeditor.entity.Customer;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomerBrowse extends AbstractLookup {
    @Inject
    private CollectionDatasource<Customer, UUID> customersDs;
    @Inject
    private Table<Customer> customersTable;

    @Override
    public void init(Map<String, Object> params) {
        customersTable.addAction(new EditAction(customersTable) {
            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity existingItem, Datasource parentDs, Map<String, Object> params) {
                Map<String, Object> editActionParams = new HashMap<>();
                editActionParams.putAll(params);
                editActionParams.put("isEditAction", true);
                editActionParams.put("navigationEditorDs", customersDs);

                super.internalOpenEditor(datasource, existingItem, parentDs, editActionParams);
            }
        });
    }
}