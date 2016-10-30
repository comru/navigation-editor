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
    private HBoxLayout navigableBox;
    @Inject
    private DataSupplier dataSupplier;
    @Inject
    private Messages messages;
    @Inject
    private FieldGroup fieldGroup;

    private NavigableLinkButton prevItemBtn;

    private NavigableLinkButton nextItemBtn;
    @Inject
    private VBoxLayout nextItemsContent;
    @Inject
    private VBoxLayout prevItemsContent;
    @Inject
    private PopupView prevItemsView;
    @Inject
    private PopupView nextItemsView;
    @Inject
    private ComponentsFactory componentsFactory;

    @Override
    public void init(Map<String, Object> params) {
        if (isEditAction && navigationEditorDs instanceof CollectionDatasource.Ordered) {
            prevItemBtn = new NavigableLinkButton();
            prevItemBtn.setDirection(Direction.PREV);
            prevItemBtn.setCaption(getMessage("prevItem"));
            navigableBox.add(prevItemBtn, 1);

            nextItemBtn = new NavigableLinkButton();
            nextItemBtn.setDirection(Direction.NEXT);
            nextItemBtn.setCaption(getMessage("nextItem"));
            navigableBox.add(nextItemBtn, 3);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void postInit() {
        if (isEditAction && navigationEditorDs instanceof CollectionDatasource.Ordered) {
            navigableBox.setVisible(true);

            UUID itemId = getItem().getId();

            UUID prevItemId = (UUID) ((CollectionDatasource.Ordered) navigationEditorDs).prevItemId(itemId);
            prevItemBtn.setEnabled(prevItemId != null);
            prevItemBtn.setCaption(getMessage("prevItem"));
            prevItemBtn.setNavigableItemId(prevItemId);
            Customer prevItem = navigationEditorDs.getItem(prevItemId);
            initNavigablePopupView(Direction.PREV, prevItemsView, prevItemsContent);
            if (prevItem != null) {
                prevItemBtn.setCaption(prevItemBtn.getCaption() + " (" + prevItem.getInstanceName() + ")");
            }

            UUID nextItemId = (UUID) ((CollectionDatasource.Ordered) navigationEditorDs).nextItemId(itemId);
            nextItemBtn.setEnabled(nextItemId != null);
            nextItemBtn.setCaption(getMessage("nextItem"));
            nextItemBtn.setNavigableItemId(nextItemId);
            Customer nextItem = navigationEditorDs.getItem(nextItemId);
            initNavigablePopupView(Direction.NEXT, nextItemsView, nextItemsContent);
            if (nextItem != null) {
                nextItemBtn.setCaption(nextItemBtn.getCaption() + " (" + nextItem.getInstanceName() + ")");
            }
        }
    }

    private void initNavigablePopupView(Direction direction,
                                        PopupView popupView, BoxLayout popupContent) {
        popupView.setEnabled(false);
        popupContent.removeAll();

        CollectionDatasource.Ordered orderedDs = ((CollectionDatasource.Ordered) navigationEditorDs);
        UUID itemId = getItem().getId();

        Object navigableItemId = direction == Direction.PREV ? orderedDs.prevItemId(itemId) : orderedDs.nextItemId(itemId);
        if (navigableItemId == null) {
            return;
        }

        for (int i = 0; i < 10 && navigableItemId != null; i++) {
            Entity navigationItem = orderedDs.getItem(navigableItemId);
            if (navigationItem == null) {
                return;
            }
            NavigableLinkButton navigableLinkButton = new NavigableLinkButton();
            navigableLinkButton.setCaption(navigationItem.getInstanceName());
            navigableLinkButton.setNavigableItemId((UUID) navigableItemId);
            navigableLinkButton.setDirection(direction);
            popupContent.add(navigableLinkButton);

            navigableItemId = direction == Direction.PREV ? orderedDs.prevItemId(navigableItemId) : orderedDs.nextItemId(navigableItemId);
        }

        if (!popupContent.getComponents().isEmpty()) {
            popupView.setEnabled(true);
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
                                fieldGroup.requestFocus();
                            }
                        }
                });
    }

    private class NavigableLinkButton extends WebLinkButton {
        private UUID navigableItemId;
        private Direction direction;

        public NavigableLinkButton() {
            super();
            NavigableLinkButton.this.setAction(new AbstractAction("navigation") {
                @Override
                public void actionPerform(Component component) {
                    navigation();
                }
            });
        }

        public UUID getNavigableItemId() {
            return navigableItemId;
        }

        public void setNavigableItemId(UUID navigableItemId) {
            this.navigableItemId = navigableItemId;
        }

        public Direction getDirection() {
            return direction;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
        }

        public void navigation() {
            if (navigableItemId == null) {
                String notifyMsg = direction == Direction.PREV ? getMessage("prevItemIsEmpty") : getMessage("nextItemIsEmpty");
                showNotification(notifyMsg, NotificationType.HUMANIZED);
                return;
            }
            replaceItem(navigableItemId);
        }
    }
}