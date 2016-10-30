package com.company.navigationeditor.web.test;

import com.company.navigationeditor.entity.Test;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class TestBrowse extends AbstractLookup {

    @Inject
    private CollectionDatasource<Test, UUID> testsDs;

    @Inject
    private Datasource<Test> testDs;

    @Inject
    private Table<Test> testsTable;

    @Inject
    private BoxLayout lookupBox;

    @Inject
    private BoxLayout actionsPane;

    @Inject
    private FieldGroup fieldGroup;
    
    @Named("testsTable.remove")
    private RemoveAction testsTableRemove;
    
    @Inject
    private DataSupplier dataSupplier;

    private boolean creating;

    @Override
    public void init(Map<String, Object> params) {
        testsDs.addItemChangeListener(e -> {
            if (e.getItem() != null) {
                Test reloadedItem = dataSupplier.reload(e.getDs().getItem(), testDs.getView());
                testDs.setItem(reloadedItem);
            }
        });
        
        testsTable.addAction(new CreateAction(testsTable) {
            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity newItem, Datasource parentDs, Map<String, Object> params) {
                testsTable.setSelected(Collections.emptyList());
                testDs.setItem((Test) newItem);
                enableEditControls(true);
            }
        });

        testsTable.addAction(new EditAction(testsTable) {
            @Override
            protected void internalOpenEditor(CollectionDatasource datasource, Entity existingItem, Datasource parentDs, Map<String, Object> params) {
                if (testsTable.getSelected().size() == 1) {
                    enableEditControls(false);
                }
            }
        });
        
        testsTableRemove.setAfterRemoveHandler(removedItems -> testDs.setItem(null));
        
        disableEditControls();
    }

    public void save() {
        getDsContext().commit();

        Test editedItem = testDs.getItem();
        if (creating) {
            testsDs.includeItem(editedItem);
        } else {
            testsDs.updateItem(editedItem);
        }
        testsTable.setSelected(editedItem);

        disableEditControls();
    }

    public void cancel() {
        Test selectedItem = testsDs.getItem();
        if (selectedItem != null) {
            Test reloadedItem = dataSupplier.reload(selectedItem, testDs.getView());
            testsDs.setItem(reloadedItem);
        } else {
            testDs.setItem(null);
        }

        disableEditControls();
    }

    private void enableEditControls(boolean creating) {
        this.creating = creating;
        initEditComponents(true);
        fieldGroup.requestFocus();
    }

    private void disableEditControls() {
        initEditComponents(false);
        testsTable.requestFocus();
    }

    private void initEditComponents(boolean enabled) {
        fieldGroup.setEditable(enabled);
        actionsPane.setVisible(enabled);
        lookupBox.setEnabled(!enabled);
    }
}