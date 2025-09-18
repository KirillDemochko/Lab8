package org.example.client.gui.components;

import org.example.client.gui.resources.Localization;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

public class LocaleComboBox extends JComboBox<String> {
    private final Localization localization;
    private final String[] localeKeys = {
            "language.russian",
            "language.portuguese",
            "language.polish",
            "language.englishNZ"
    };
    private ActionListener actionListener; // Сохраняем слушатель для управления

    public LocaleComboBox() {
        this.localization = Localization.getInstance();
        updateItems();
        updateSelectedItem();
        System.out.println("LocaleComboBox: Initialized with " + getItemCount() + " items");
        actionListener = e -> {
            System.out.println("LocaleComboBox: Action performed, selected index: " + getSelectedIndex());
            changeLocale();
        };
        addActionListener(actionListener);
        localization.addLocaleChangeListener(newLocale -> {
            System.out.println("LocaleComboBox: Locale changed to " + newLocale);
            updateItems();
            updateSelectedItem();
        });
    }

    private void updateItems() {
        removeActionListener(actionListener); // Отключаем слушатель
        removeAllItems();
        for (String key : localeKeys) {
            addItem(localization.getString(key));
        }
        addActionListener(actionListener); // Включаем слушатель обратно
        System.out.println("LocaleComboBox: Updated items, count: " + getItemCount());
    }

    public void updateSelectedItem() {
        removeActionListener(actionListener); // Отключаем слушатель
        Locale currentLocale = localization.getCurrentLocale();
        int selectedIndex = -1;
        if (currentLocale.equals(Localization.RUSSIAN)) {
            selectedIndex = 0;
        } else if (currentLocale.equals(Localization.PORTUGUESE)) {
            selectedIndex = 1;
        } else if (currentLocale.equals(Localization.POLISH)) {
            selectedIndex = 2;
        } else if (currentLocale.equals(Localization.ENGLISH_NZ)) {
            selectedIndex = 3;
        }

        if (selectedIndex >= 0 && selectedIndex < getItemCount()) {
            setSelectedIndex(selectedIndex);
        }

        addActionListener(actionListener); // Включаем слушатель обратно
    }

    private void changeLocale() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex == -1) {
            System.err.println("LocaleComboBox: No item selected");
            return;
        }
        System.out.println("LocaleComboBox: Changing locale to index " + selectedIndex);
        Locale newLocale;
        switch (selectedIndex) {
            case 0:
                newLocale = Localization.RUSSIAN;
                break;
            case 1:
                newLocale = Localization.PORTUGUESE;
                break;
            case 2:
                newLocale = Localization.POLISH;
                break;
            case 3:
                newLocale = Localization.ENGLISH_NZ;
                break;
            default:
                return;
        }
        if (!newLocale.equals(localization.getCurrentLocale())) {
            localization.setLocale(newLocale);
        } else {
            System.out.println("LocaleComboBox: Skipping setLocale, already " + newLocale);
        }
    }
}