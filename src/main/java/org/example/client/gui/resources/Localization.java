package org.example.client.gui.resources;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Localization {
    private static Localization instance;
    private Locale currentLocale;
    private ResourceBundle resourceBundle;
    private Map<Locale, ResourceBundle> bundles;
    private List<LocaleChangeListener> localeChangeListeners;

    public static final Locale RUSSIAN = new Locale("ru", "RU");
    public static final Locale PORTUGUESE = new Locale("pt", "PT");
    public static final Locale POLISH = new Locale("pl", "PL");
    public static final Locale ENGLISH_NZ = new Locale("en", "NZ");

    private Localization() {
        bundles = new HashMap<>();
        localeChangeListeners = new ArrayList<>();

        bundles.put(RUSSIAN, new RussianBundle());
        bundles.put(PORTUGUESE, new PortugueseBundle());
        bundles.put(POLISH, new PolishBundle());
        bundles.put(ENGLISH_NZ, new EnglishNZBundle());

        setLocale(RUSSIAN);
    }

    public static Localization getInstance() {
        if (instance == null) {
            instance = new Localization();
        }
        return instance;
    }

    public void setLocale(Locale locale) {
        if (bundles.containsKey(locale)) {
            this.currentLocale = locale;
            this.resourceBundle = bundles.get(locale);
            System.out.println("Localization: Set locale to " + locale);
            notifyLocaleChangeListeners();
        } else {
            throw new IllegalArgumentException("Unsupported locale: " + locale);
        }
    }


    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    public String getString(String key, boolean returnKeyIfMissing) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return returnKeyIfMissing ? key : "!" + key + "!";
        }
    }

    public String formatNumber(Number number) {
        return NumberFormat.getNumberInstance(currentLocale).format(number);
    }

    public String formatDate(ZonedDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                getString("date.format"), currentLocale
        );
        return date.format(formatter);
    }

    public void addLocaleChangeListener(LocaleChangeListener listener) {
        localeChangeListeners.add(listener);
    }

    public void removeLocaleChangeListener(LocaleChangeListener listener) {
        localeChangeListeners.remove(listener);
    }

    private void notifyLocaleChangeListeners() {
        System.out.println("Localization: Notifying " + localeChangeListeners.size() + " listeners");
        for (LocaleChangeListener listener : localeChangeListeners) {
            listener.onLocaleChanged(currentLocale);
        }
    }

    public interface LocaleChangeListener {
        void onLocaleChanged(Locale newLocale);
    }

    private static class RussianBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                    {"application.title", "Клиент коллекции продуктов"},
                    {"login.title", "Авторизация"},
                    {"login.username", "Имя пользователя"},
                    {"login.password", "Пароль"},
                    {"login.button", "Войти"},
                    {"register.button", "Зарегистрироваться"},
                    {"login.error", "Ошибка авторизации"},
                    {"welcome", "Добро пожаловать, {0}"},
                    {"menu.file", "Файл"},
                    {"menu.exit", "Выход"},
                    {"menu.view", "Вид"},
                    {"menu.language", "Язык"},
                    {"menu.commands", "Команды"},
                    {"table.header.id", "ID"},
                    {"table.header.name", "Название"},
                    {"table.header.coordinates", "Координаты"},
                    {"table.header.creationDate", "Дата создания"},
                    {"table.header.price", "Цена"},
                    {"table.header.partNumber", "Парт-номер"},
                    {"table.header.manufactureCost", "Стоимость производства"},
                    {"table.header.unit", "Единица измерения"},
                    {"table.header.manufacturer", "Производитель"},
                    {"table.header.creator", "Создатель"},
                    {"button.add", "Добавить"},
                    {"button.update", "Обновить"},
                    {"button.remove", "Удалить"},
                    {"button.filter", "Фильтр"},
                    {"button.clear", "Очистить"},
                    {"button.execute", "Выполнить скрипт"},
                    {"button.refresh", "Обновить"},
                    {"date.format", "dd.MM.yyyy HH:mm:ss"},
                    {"status.connected", "Подключено"},
                    {"status.lastUpdate", "Последнее обновление: "},
                    {"status.error", "Ошибка: "},
                    {"error.title", "Ошибка"},
                    {"error.selectProduct", "Пожалуйста, выберите продукт"},
                    {"error.editOtherUser", "Вы можете редактировать только свои продукты"},
                    {"error.deleteOtherUser", "Вы можете удалять только свои продукты"},
                    {"confirm.title", "Подтверждение"},
                    {"confirm.delete", "Вы уверены, что хотите удалить этот продукт?"},
                    {"confirm.clear", "Вы уверены, что хотите очистить все ваши продукты?"},
                    {"success.title", "Успех"},
                    {"script.title", "Выполнение скрипта"},
                    {"script.error", "Ошибка скрипта"},
                    {"language.changeHint", "Используйте выпадающий список на панели инструментов для изменения языка"},
                    {"language.russian", "Русский"},
                    {"language.portuguese", "Português"},
                    {"language.polish", "Polski"},
                    {"language.englishNZ", "English (NZ)"},
                    {"filter.title", "Фильтр продуктов"},
                    {"filter.field", "Поле:"},
                    {"filter.operator", "Оператор:"},
                    {"filter.value", "Значение:"},
                    {"filter.apply", "Применить фильтр"},
                    {"filter.clear", "Очистить фильтр"},
                    {"filter.error", "Ошибка фильтра: "},
                    {"filter.field.name", "Название"},
                    {"filter.field.price", "Цена"},
                    {"filter.field.manufactureCost", "Стоимость производства"},
                    {"filter.field.partNumber", "Парт-номер"},
                    {"filter.field.creatorId", "ID создателя"},
                    {"filter.operator.equals", "Равно"},
                    {"filter.operator.contains", "Содержит"},
                    {"filter.operator.greater", "Больше чем"},
                    {"filter.operator.less", "Меньше чем"},
                    {"product.add.title", "Добавление продукта"},
                    {"product.edit.title", "Редактирование продукта"},
                    {"label.name", "Название"},
                    {"label.coordinates.x", "Координата X"},
                    {"label.coordinates.y", "Координата Y"},
                    {"label.price", "Цена"},
                    {"label.partNumber", "Парт-номер"},
                    {"label.manufactureCost", "Стоимость производства"},
                    {"label.unit", "Единица измерения"},
                    {"label.manufacturer", "Производитель"},
                    {"button.save", "Сохранить"},
                    {"button.cancel", "Отмена"},
                    {"error.xCoordinate.invalid", "Координата X должна быть больше -349"},
                    {"error.yCoordinate.invalid", "Координата Y должна быть не больше 678"},
                    {"error.price.invalid", "Цена должна быть больше 0"},
                    {"error.partNumber.empty", "Парт-номер не может быть пустым"},
                    {"error.partNumber.length", "Длина парт-номера должна быть не менее 21 символа"},
                    {"error.manufactureCost.invalid", "Стоимость производства должна быть положительным числом"},
                    {"error.name.empty", "Название не может быть пустым"},
                    {"option.yes", "Да"},
                    {"option.no", "Нет"},
            };
        }
    }

    private static class PortugueseBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                    {"application.title", "Cliente de Coleção de Produtos"},
                    {"login.title", "Autenticação"},
                    {"login.username", "Nome de usuário"},
                    {"login.password", "Senha"},
                    {"login.button", "Entrar"},
                    {"register.button", "Registrar"},
                    {"login.error", "Erro de autenticação"},
                    {"welcome", "Bem-vindo, {0}"},
                    {"menu.file", "Arquivo"},
                    {"menu.exit", "Sair"},
                    {"menu.view", "Visualizar"},
                    {"menu.language", "Idioma"},
                    {"menu.commands", "Comandos"},
                    {"table.header.id", "ID"},
                    {"table.header.name", "Nome"},
                    {"table.header.coordinates", "Coordenadas"},
                    {"table.header.creationDate", "Data de criação"},
                    {"table.header.price", "Preço"},
                    {"table.header.partNumber", "Número de peça"},
                    {"table.header.manufactureCost", "Custo de fabricação"},
                    {"table.header.unit", "Unidade de medida"},
                    {"table.header.manufacturer", "Fabricante"},
                    {"table.header.creator", "Criador"},
                    {"button.add", "Adicionar"},
                    {"button.update", "Atualizar"},
                    {"button.remove", "Remover"},
                    {"button.filter", "Filtrar"},
                    {"button.clear", "Limpar"},
                    {"button.execute", "Executar script"},
                    {"button.refresh", "Atualizar"},
                    {"date.format", "dd/MM/yyyy HH:mm:ss"},
                    {"status.connected", "Conectado"},
                    {"status.lastUpdate", "Última atualização: "},
                    {"status.error", "Erro: "},
                    {"error.title", "Erro"},
                    {"error.selectProduct", "Por favor, selecione um produto"},
                    {"error.editOtherUser", "Você só pode editar seus próprios produtos"},
                    {"error.deleteOtherUser", "Você só pode excluir seus próprios produtos"},
                    {"confirm.title", "Confirmação"},
                    {"confirm.delete", "Tem certeza de que deseja excluir este produto?"},
                    {"confirm.clear", "Tem certeza de que deseja limpar todos os seus produtos?"},
                    {"success.title", "Sucesso"},
                    {"script.title", "Execução de script"},
                    {"script.error", "Erro de script"},
                    {"language.changeHint", "Use a lista suspensa na barra de ferramentas para alterar o idioma"},
                    {"language.russian", "Русский"},
                    {"language.portuguese", "Português"},
                    {"language.polish", "Polski"},
                    {"language.englishNZ", "English (NZ)"},
                    {"filter.title", "Filtrar Produtos"},
                    {"filter.field", "Campo:"},
                    {"filter.operator", "Operador:"},
                    {"filter.value", "Valor:"},
                    {"filter.apply", "Aplicar Filtro"},
                    {"filter.clear", "Limpar Filtro"},
                    {"filter.error", "Erro de filtro: "},
                    {"filter.field.name", "Nome"},
                    {"filter.field.price", "Preço"},
                    {"filter.field.manufactureCost", "Custo de fabricação"},
                    {"filter.field.partNumber", "Número de peça"},
                    {"filter.field.creatorId", "ID do criador"},
                    {"filter.operator.equals", "Igual"},
                    {"filter.operator.contains", "Contém"},
                    {"filter.operator.greater", "Maior que"},
                    {"filter.operator.less", "Menor que"},
                    {"product.add.title", "Adicionar Produto"},
                    {"product.edit.title", "Editar Produto"},
                    {"label.name", "Nome"},
                    {"label.coordinates.x", "Coordenada X"},
                    {"label.coordinates.y", "Coordenada Y"},
                    {"label.price", "Preço"},
                    {"label.partNumber", "Número de peça"},
                    {"label.manufactureCost", "Custo de fabricação"},
                    {"label.unit", "Unidade de medida"},
                    {"label.manufacturer", "Fabricante"},
                    {"button.save", "Salvar"},
                    {"button.cancel", "Cancelar"},
                    {"error.xCoordinate.invalid", "A coordenada X deve ser maior que -349"},
                    {"error.yCoordinate.invalid", "A coordenada Y não pode ser maior que 678"},
                    {"error.price.invalid", "O preço deve ser maior que 0"},
                    {"error.partNumber.empty", "O número da peça não pode estar vazio"},
                    {"error.partNumber.length", "O número da peça deve ter pelo menos 21 caracteres"},
                    {"error.manufactureCost.invalid", "O custo de fabricação deve ser um número positivo"},
                    {"error.name.empty", "O nome não pode estar vazio"},
                    {"option.yes", "Sim"},
                    {"option.no", "Não"},
            };
        }
    }

    private static class PolishBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                    {"application.title", "Klient Kolekcji Produktów"},
                    {"login.title", "Uwierzytelnianie"},
                    {"login.username", "Nazwa użytkownika"},
                    {"login.password", "Hasło"},
                    {"login.button", "Zaloguj"},
                    {"register.button", "Zarejestruj"},
                    {"login.error", "Błąd uwierzytelniania"},
                    {"welcome", "Witaj, {0}"},
                    {"menu.file", "Plik"},
                    {"menu.exit", "Wyjście"},
                    {"menu.view", "Widok"},
                    {"menu.language", "Język"},
                    {"menu.commands", "Polecenia"},
                    {"table.header.id", "ID"},
                    {"table.header.name", "Nazwa"},
                    {"table.header.coordinates", "Współrzędne"},
                    {"table.header.creationDate", "Data utworzenia"},
                    {"table.header.price", "Cena"},
                    {"table.header.partNumber", "Numer części"},
                    {"table.header.manufactureCost", "Koszt produkcji"},
                    {"table.header.unit", "Jednostka miary"},
                    {"table.header.manufacturer", "Producent"},
                    {"table.header.creator", "Twórca"},
                    {"button.add", "Dodaj"},
                    {"button.update", "Aktualizuj"},
                    {"button.remove", "Usuń"},
                    {"button.filter", "Filtruj"},
                    {"button.clear", "Wyczyść"},
                    {"button.execute", "Wykonaj skrypt"},
                    {"button.refresh", "Odśwież"},
                    {"date.format", "dd.MM.yyyy HH:mm:ss"},
                    {"status.connected", "Połączono"},
                    {"status.lastUpdate", "Ostatnia aktualizacja: "},
                    {"status.error", "Błąd: "},
                    {"error.title", "Błąd"},
                    {"error.selectProduct", "Proszę wybrać produkt"},
                    {"error.editOtherUser", "Możesz edytować tylko swoje produkty"},
                    {"error.deleteOtherUser", "Możesz usuwać tylko swoje produkty"},
                    {"confirm.title", "Potwierdzenie"},
                    {"confirm.delete", "Czy na pewno chcesz usunąć ten produkt?"},
                    {"confirm.clear", "Czy na pewno chcesz wyczyścić wszystkie swoje produkty?"},
                    {"success.title", "Sukces"},
                    {"script.title", "Wykonywanie skryptu"},
                    {"script.error", "Błąd skryptu"},
                    {"language.changeHint", "Użyj listy rozwijanej na pasku narzędzi, aby zmienić język"},
                    {"language.russian", "Русский"},
                    {"language.portuguese", "Português"},
                    {"language.polish", "Polski"},
                    {"language.englishNZ", "English (NZ)"},
                    {"filter.title", "Filtruj Produkty"},
                    {"filter.field", "Pole:"},
                    {"filter.operator", "Operator:"},
                    {"filter.value", "Wartość:"},
                    {"filter.apply", "Zastosuj filtr"},
                    {"filter.clear", "Wyczyść filtr"},
                    {"filter.error", "Błąd filtra: "},
                    {"filter.field.name", "Nazwa"},
                    {"filter.field.price", "Cena"},
                    {"filter.field.manufactureCost", "Koszt produkcji"},
                    {"filter.field.partNumber", "Numer części"},
                    {"filter.field.creatorId", "ID twórcy"},
                    {"filter.operator.equals", "Równe"},
                    {"filter.operator.contains", "Zawiera"},
                    {"filter.operator.greater", "Większe niż"},
                    {"filter.operator.less", "Mniejsze niż"},
                    {"product.add.title", "Dodaj Produkt"},
                    {"product.edit.title", "Edytuj Produkt"},
                    {"label.name", "Nazwa"},
                    {"label.coordinates.x", "Współrzędna X"},
                    {"label.coordinates.y", "Współrzędna Y"},
                    {"label.price", "Cena"},
                    {"label.partNumber", "Numer części"},
                    {"label.manufactureCost", "Koszt produkcji"},
                    {"label.unit", "Jednostka miary"},
                    {"label.manufacturer", "Producent"},
                    {"button.save", "Zapisz"},
                    {"button.cancel", "Anuluj"},
                    {"error.xCoordinate.invalid", "Współrzędna X musi być większa niż -349"},
                    {"error.yCoordinate.invalid", "Współrzędna Y nie może być większa niż 678"},
                    {"error.price.invalid", "Cena musi być większa niż 0"},
                    {"error.partNumber.empty", "Numer części nie może być pusty"},
                    {"error.partNumber.length", "Numer części musi mieć co najmniej 21 znaków"},
                    {"error.manufactureCost.invalid", "Koszt produkcji musi być dodatnią liczbą"},
                    {"error.name.empty", "Nazwa nie może być pusta"},
                    {"option.yes", "Tak"},
                    {"option.no", "Nie"},
            };
        }
    }

    private static class EnglishNZBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                    {"application.title", "Product Collection Client"},
                    {"login.title", "Authentication"},
                    {"login.username", "Username"},
                    {"login.password", "Password"},
                    {"login.button", "Login"},
                    {"register.button", "Register"},
                    {"login.error", "Authentication error"},
                    {"welcome", "Welcome, {0}"},
                    {"menu.file", "File"},
                    {"menu.exit", "Exit"},
                    {"menu.view", "View"},
                    {"menu.language", "Language"},
                    {"menu.commands", "Commands"},
                    {"table.header.id", "ID"},
                    {"table.header.name", "Name"},
                    {"table.header.coordinates", "Coordinates"},
                    {"table.header.creationDate", "Creation Date"},
                    {"table.header.price", "Price"},
                    {"table.header.partNumber", "Part Number"},
                    {"table.header.manufactureCost", "Manufacture Cost"},
                    {"table.header.unit", "Unit of Measure"},
                    {"table.header.manufacturer", "Manufacturer"},
                    {"table.header.creator", "Creator"},
                    {"button.add", "Add"},
                    {"button.update", "Update"},
                    {"button.remove", "Remove"},
                    {"button.filter", "Filter"},
                    {"button.clear", "Clear"},
                    {"button.execute", "Execute Script"},
                    {"button.refresh", "Refresh"},
                    {"date.format", "dd/MM/yyyy HH:mm:ss"},
                    {"status.connected", "Connected"},
                    {"status.lastUpdate", "Last update: "},
                    {"status.error", "Error: "},
                    {"error.title", "Error"},
                    {"error.selectProduct", "Please select a product"},
                    {"error.editOtherUser", "You can only edit your own products"},
                    {"error.deleteOtherUser", "You can only delete your own products"},
                    {"confirm.title", "Confirmation"},
                    {"confirm.delete", "Are you sure you want to delete this product?"},
                    {"confirm.clear", "Are you sure you want to clear all your products?"},
                    {"success.title", "Success"},
                    {"script.title", "Script Execution"},
                    {"script.error", "Script Error"},
                    {"language.changeHint", "Use the dropdown list on the toolbar to change language"},
                    {"language.russian", "Русский"},
                    {"language.portuguese", "Português"},
                    {"language.polish", "Polski"},
                    {"language.englishNZ", "English (NZ)"},
                    {"filter.title", "Filter Products"},
                    {"filter.field", "Field:"},
                    {"filter.operator", "Operator:"},
                    {"filter.value", "Value:"},
                    {"filter.apply", "Apply Filter"},
                    {"filter.clear", "Clear Filter"},
                    {"filter.error", "Filter error: "},
                    {"filter.field.name", "Name"},
                    {"filter.field.price", "Price"},
                    {"filter.field.manufactureCost", "Manufacture Cost"},
                    {"filter.field.partNumber", "Part Number"},
                    {"filter.field.creatorId", "Creator ID"},
                    {"filter.operator.equals", "Equals"},
                    {"filter.operator.contains", "Contains"},
                    {"filter.operator.greater", "Greater than"},
                    {"filter.operator.less", "Less than"},
                    {"product.add.title", "Add Product"},
                    {"product.edit.title", "Edit Product"},
                    {"label.name", "Name"},
                    {"label.coordinates.x", "Coordinate X"},
                    {"label.coordinates.y", "Coordinate Y"},
                    {"label.price", "Price"},
                    {"label.partNumber", "Part Number"},
                    {"label.manufactureCost", "Manufacture Cost"},
                    {"label.unit", "Unit of Measure"},
                    {"label.manufacturer", "Manufacturer"},
                    {"button.save", "Save"},
                    {"button.cancel", "Cancel"},
                    {"error.xCoordinate.invalid", "Coordinate X must be greater than -349"},
                    {"error.yCoordinate.invalid", "Coordinate Y cannot be greater than 678"},
                    {"error.price.invalid", "Price must be greater than 0"},
                    {"error.partNumber.empty", "Part number cannot be empty"},
                    {"error.partNumber.length", "Part number must be at least 21 characters long"},
                    {"error.manufactureCost.invalid", "Manufacture cost must be a positive number"},
                    {"error.name.empty", "Name cannot be empty"},
                    {"option.yes", "Yes"},
                    {"option.no", "No"},
            };
        }
    }
}