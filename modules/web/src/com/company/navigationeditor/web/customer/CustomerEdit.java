package com.company.navigationeditor.web.customer;

import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class CustomerEdit extends AbstractEditor<Customer> {
    private enum Direction {
        PREV, NEXT
    }

    @WindowParam
    private boolean isEditAction;
    @WindowParam
    private CollectionDatasource<Customer, UUID> navigationEditorDs;

    @Inject
    private Metadata metadata;
    @Inject
    private Datasource<Customer> customerDs;
    @Inject
    private HBoxLayout navigationalBox;
    @Inject
    private DataSupplier dataSupplier;
    @Inject
    private Messages messages;
    @Inject
    private FieldGroup fieldGroup;
    @Inject
    private ComponentsFactory componentsFactory;
    @Inject
    private LookupField goToField;
    @Inject
    private Button createItemBtn;
    @Inject
    private Button applyItemBtn;

    private NavigationalLinkButton prevItemBtn;
    private NavigationalLinkButton nextItemBtn;
    private boolean replacing = false;

    @Override
    public void init(Map<String, Object> params) {
        if (isEditAction && navigationEditorDs instanceof CollectionDatasource.Ordered) {
            navigationalBox.setVisible(true);
            createItemBtn.setVisible(true);
            applyItemBtn.setVisible(true);

            goToField.setOptionsDatasource(navigationEditorDs);
            goToField.addValueChangeListener(e -> {
                if (replacing) {
                    return;
                }
                Customer selectedItem = (Customer) e.getValue();
                replacing = true;
                try {
                    replaceItem(selectedItem);
                } finally {
                    replacing = false;
                }
            });

            prevItemBtn = new NavigationalLinkButton();
            prevItemBtn.setAlignment(Alignment.MIDDLE_CENTER);
            prevItemBtn.setDirection(Direction.PREV);
            prevItemBtn.setCaption(getMessage("prevItem"));
            navigationalBox.add(prevItemBtn, 0);

            nextItemBtn = new NavigationalLinkButton();
            nextItemBtn.setAlignment(Alignment.MIDDLE_CENTER);
            nextItemBtn.setDirection(Direction.NEXT);
            nextItemBtn.setCaption(getMessage("nextItem"));
            navigationalBox.add(nextItemBtn, 2);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void postInit() {
        if (isEditAction && navigationEditorDs instanceof CollectionDatasource.Ordered) {
            UUID itemId = getItem().getId();

            UUID prevItemId = (UUID) ((CollectionDatasource.Ordered) navigationEditorDs).prevItemId(itemId);
            prevItemBtn.setEnabled(prevItemId != null);
            prevItemBtn.setCaption(getMessage("prevItem"));
            prevItemBtn.setNavigationalItemId(prevItemId);

            UUID nextItemId = (UUID) ((CollectionDatasource.Ordered) navigationEditorDs).nextItemId(itemId);
            nextItemBtn.setEnabled(nextItemId != null);
            nextItemBtn.setCaption(getMessage("nextItem"));
            nextItemBtn.setNavigationalItemId(nextItemId);

            goToField.setValue(getItem());
        }
    }

    public void applyItem() {
        if (isModified()) {
            commit();
            Customer customer = getItem();
            showNotification(messages.formatMessage(AppConfig.getMessagesPack(), "info.EntitySave",
                    messages.getTools().getEntityCaption(customer.getMetaClass()), customer.getInstanceName()),
                    NotificationType.TRAY);
        } else {
            showNotification(getMessage("noChanges"), NotificationType.TRAY);
        }
    }

    public void createItem() {
        Customer newItem = metadata.create(Customer.class);
        initEmbeddedFields(newItem);
        AbstractEditor abstractEditor = openEditor(newItem, WindowManager.OpenType.DIALOG, Collections.emptyMap());
        abstractEditor.addCloseWithCommitListener(() -> {
            Customer committedItem = (Customer) abstractEditor.getItem();
            navigationEditorDs.addItem(committedItem);
            goToField.setValue(committedItem);
        });
    }

    //todo copy-paste see #com.haulmont.cuba.gui.components.actions.CreateAction.actionPerform()
    private void initEmbeddedFields(Customer item) {
        // instantiate embedded fields
        DataSupplier dataservice = customerDs.getDataSupplier();
        Collection<MetaProperty> properties = item.getMetaClass().getProperties();
        for (MetaProperty property : properties) {
            if (!property.isReadOnly() && property.getAnnotations().containsKey("embedded")) {
                if (item.getValue(property.getName()) == null) {
                    Entity defaultEmbeddedInstance = dataservice.newInstance(property.getRange().asClass());
                    item.setValue(property.getName(), defaultEmbeddedInstance);
                }
            }
        }
    }

    private void replaceItem(Customer goToItem) {
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

    private class NavigationalLinkButton extends WebLinkButton {
        private UUID navigationalItemId;
        private Direction direction;

        public NavigationalLinkButton() {
            super();
            NavigationalLinkButton.this.setAction(new AbstractAction("navigation") {
                @Override
                public void actionPerform(Component component) {
                    navigation();
                }
            });
        }

        public void setNavigationalItemId(UUID navigationalItemId) {
            this.navigationalItemId = navigationalItemId;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
        }

        public void navigation() {
            if (navigationalItemId == null) {
                String notifyMsg = direction == Direction.PREV ? getMessage("prevItemIsEmpty") : getMessage("nextItemIsEmpty");
                showNotification(notifyMsg, NotificationType.HUMANIZED);
                return;
            }
            goToField.setValue(navigationEditorDs.getItem(navigationalItemId));
        }
    }
}