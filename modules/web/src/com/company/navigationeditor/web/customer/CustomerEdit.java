package com.company.navigationeditor.web.customer;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.company.navigationeditor.entity.Customer;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.theme.ThemeConstantsManager;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.cuba.web.gui.components.WebLinkButton;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

public class CustomerEdit extends AbstractEditor<Customer> {
    private enum Direction {
        PREV, NEXT;
    }

    @WindowParam
    private boolean isEditAction;
    @WindowParam
    private CollectionDatasource<Customer, UUID> navigationEditorDs;
    @Inject
    private Datasource<Customer> customerDs;
    @Inject
    private DataSupplier dataSupplier;
    @Inject
    private Messages messages;
    @Inject
    private FieldGroup fieldGroup;
    @Inject
    private PopupView prevItemsView;
    @Inject
    private PopupView nextItemsView;
    @Inject
    private ComponentsFactory componentsFactory;
    @Inject
    private LookupField goToField;
    @Inject
    private HBoxLayout goToBox;
    private boolean replacing = false;

    @Override
    public void init(Map<String, Object> params) {
        if (isEditAction && navigationEditorDs instanceof CollectionDatasource.Ordered) {
            goToBox.setVisible(true);
            goToField.setOptionsDatasource(navigationEditorDs);
            goToField.addValueChangeListener(e -> {
                if (e.getValue() == null || replacing) {
                    return;
                }
                Customer selectedItem = (Customer) e.getValue();
                replacing = true;
                try {
                    replaceItem(selectedItem.getId());
                } finally {
                    replacing = false;
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void postInit() {
        if (isEditAction && navigationEditorDs instanceof CollectionDatasource.Ordered) {
            goToField.setValue(getItem());
        }
    }

    private void replaceItem(UUID goToItemId) {
        Customer goToItem = navigationEditorDs.getItem(goToItemId);
        Customer reloadedGoToItem = dataSupplier.reload(goToItem, customerDs.getView());
        onReplaceItem(() -> setItem(reloadedGoToItem), goToItem);
    }

    private void onReplaceItem(Runnable replaceAction, Customer goToItem) {
        if (!isModified()) {
            replaceAction.run();
            return;
        }

        showOptionDialog(
                messages.getMainMessage("closeUnsaved.caption"),
                formatMessage("saveUnsaved", goToItem.getInstanceName()),
                MessageType.WARNING,
                new Action[]{
                        new DialogAction(DialogAction.Type.OK, Action.Status.PRIMARY) {
                            @Override
                            public String getCaption() {
                                return messages.getMainMessage("closeUnsaved.save");
                            }

                            @Override
                            public void actionPerform(Component component) {
                                commit();
                                //todo performance issue
                                navigationEditorDs.refresh();
                                replaceAction.run();
                            }
                        },
                        new AbstractAction("discard") {
                            {
                                ThemeConstantsManager thCM = AppBeans.get(ThemeConstantsManager.NAME);
                                icon = thCM.getThemeValue("actions.dialog.Cancel.icon");
                            }

                            @Override
                            public String getCaption() {
                                return messages.getMainMessage("closeUnsaved.discard");
                            }

                            @Override
                            public void actionPerform(Component component) {
                                replaceAction.run();
                            }
                        },
                        new DialogAction(DialogAction.Type.CANCEL) {
                            @Override
                            public String getIcon() {
                                return null;
                            }

                            @Override
                            public void actionPerform(Component component) {
                                replacing = true;
                                try {
                                    goToField.setValue(getItem());
                                    fieldGroup.requestFocus();
                                } finally {
                                    replacing = false;
                                }
                            }
                        }
                });
    }
}