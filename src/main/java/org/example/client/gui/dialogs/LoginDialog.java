package org.example.client.gui.dialogs;

import org.example.client.gui.resources.Localization;
import org.example.client.network.GuiClient;
import org.example.client.state.SessionState;
import org.example.data.User;
import org.example.util.HashUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

public class LoginDialog extends JDialog implements Localization.LocaleChangeListener {
    private final Localization localization;
    private final GuiClient client;
    private final SessionState sessionState;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JComboBox<String> languageComboBox;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel titleLabel;
    private JLabel languageLabel;

    public LoginDialog(Frame owner) {
        super(owner, true);
        this.localization = Localization.getInstance();
        this.client = GuiClient.getInstance();
        this.sessionState = SessionState.getInstance();

        initComponents();
        setTitle(localization.getString("login.title"));
        pack();
        setLocationRelativeTo(owner);

        localization.addLocaleChangeListener(this);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title label
        titleLabel = new JLabel(localization.getString("login.title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Language panel
        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        languageLabel = new JLabel(localization.getString("menu.language") + ":");
        languageComboBox = new JComboBox<>(new String[]{
                localization.getString("language.russian"),
                localization.getString("language.portuguese"),
                localization.getString("language.polish"),
                localization.getString("language.englishNZ")
        });
        languageComboBox.addActionListener(this::languageChanged);
        languagePanel.add(languageLabel);
        languagePanel.add(languageComboBox);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        usernameLabel = new JLabel(localization.getString("login.username"));
        formPanel.add(usernameLabel);
        usernameField = new JTextField(15);
        formPanel.add(usernameField);

        passwordLabel = new JLabel(localization.getString("login.password"));
        formPanel.add(passwordLabel);
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        loginButton = new JButton(localization.getString("login.button"));
        registerButton = new JButton(localization.getString("register.button"));

        loginButton.addActionListener(this::loginAction);
        registerButton.addActionListener(this::registerAction);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        formPanel.add(new JLabel()); // Empty cell
        formPanel.add(buttonPanel);

        mainPanel.add(languagePanel, BorderLayout.CENTER);
        mainPanel.add(formPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void languageChanged(ActionEvent e) {
        if ("comboBoxChanged".equals(e.getActionCommand())) {
            String selected = (String) languageComboBox.getSelectedItem();
            if (selected == null) return;

            Locale newLocale = null;
            if (selected.equals(localization.getString("language.russian"))) {
                newLocale = Localization.RUSSIAN;
            } else if (selected.equals(localization.getString("language.portuguese"))) {
                newLocale = Localization.PORTUGUESE;
            } else if (selected.equals(localization.getString("language.polish"))) {
                newLocale = Localization.POLISH;
            } else if (selected.equals(localization.getString("language.englishNZ"))) {
                newLocale = Localization.ENGLISH_NZ;
            }

            if (newLocale != null && !newLocale.equals(localization.getCurrentLocale())) {
                localization.setLocale(newLocale);
            }
        }
    }

    private void updateLocalizedText() {
        // Временно удаляем слушатель комбобокса
        ActionListener[] listeners = languageComboBox.getActionListeners();
        for (ActionListener listener : listeners) {
            languageComboBox.removeActionListener(listener);
        }

        // Обновляем тексты
        setTitle(localization.getString("login.title"));
        titleLabel.setText(localization.getString("login.title"));
        usernameLabel.setText(localization.getString("login.username"));
        passwordLabel.setText(localization.getString("login.password"));
        loginButton.setText(localization.getString("login.button"));
        registerButton.setText(localization.getString("register.button"));
        languageLabel.setText(localization.getString("menu.language") + ":");

        languageComboBox.removeAllItems();
        languageComboBox.addItem(localization.getString("language.russian"));
        languageComboBox.addItem(localization.getString("language.portuguese"));
        languageComboBox.addItem(localization.getString("language.polish"));
        languageComboBox.addItem(localization.getString("language.englishNZ"));

        Locale currentLocale = localization.getCurrentLocale();
        if (currentLocale.equals(Localization.RUSSIAN)) {
            languageComboBox.setSelectedIndex(0);
        } else if (currentLocale.equals(Localization.PORTUGUESE)) {
            languageComboBox.setSelectedIndex(1);
        } else if (currentLocale.equals(Localization.POLISH)) {
            languageComboBox.setSelectedIndex(2);
        } else if (currentLocale.equals(Localization.ENGLISH_NZ)) {
            languageComboBox.setSelectedIndex(3);
        }

        // Восстанавливаем слушатель
        for (ActionListener listener : listeners) {
            languageComboBox.addActionListener(listener);
        }
    }

    private void loginAction(ActionEvent e) {
        authenticate(false);
    }

    private void registerAction(ActionEvent e) {
        authenticate(true);
    }

    private void authenticate(boolean isRegistration) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    localization.getString("login.error.empty"),
                    localization.getString("error.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        client.sendAuthRequest(username, password, isRegistration, response -> {
            setCursor(Cursor.getDefaultCursor());
            loginButton.setEnabled(true);
            registerButton.setEnabled(true);

            if (response.isSuccess()) {
                int userId = (Integer) response.getData();
                User user = new User(userId, username, HashUtil.sha256(password));
                sessionState.setCurrentUser(user);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        response.getMessage(),
                        localization.getString("login.error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static boolean showLoginDialog(Frame owner) {
        LoginDialog dialog = new LoginDialog(owner);
        dialog.setVisible(true);
        return SessionState.getInstance().getCurrentUser() != null;
    }

    @Override
    public void onLocaleChanged(Locale newLocale) {
        updateLocalizedText();
    }
}