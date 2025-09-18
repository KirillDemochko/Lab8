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
                    {"application.title", "Клиентское приложение"},
                    {"menu.file", "Файл"},
                    {"menu.exit", "Выход"},
                    {"menu.view", "Вид"},
                    {"menu.language", "Язык"},
                    {"menu.commands", "Команды"},
                    {"button.add", "Добавить"},
                    {"button.update", "Обновить"},
                    {"button.remove", "Удалить"},
                    {"button.filter", "Фильтр"},
                    {"button.clear", "Очистить"},
                    {"button.execute", "Выполнить"},
                    {"button.save", "Сохранить"},
                    {"button.cancel", "Отмена"},
                    {"button.sort", "Сортировать"},
                    {"welcome", "Добро пожаловать, {0}"},
                    {"status.connected", "Подключено"},
                    {"status.lastUpdate", "Последнее обновление: "},
                    {"status.error", "Ошибка: "},
                    {"error.title", "Ошибка"},
                    {"error.selectProduct", "Выберите продукт"},
                    {"error.editOtherUser", "Вы можете редактировать только свои продукты"},
                    {"error.deleteOtherUser", "Вы можете удалять только свои продукты"},
                    {"confirm.title", "Подтверждение"},
                    {"confirm.delete", "Вы уверены, что хотите удалить этот продукт?"},
                    {"confirm.clear", "Вы уверены, что хотите очистить все продукты?"},
                    {"option.yes", "Да"},
                    {"option.no", "Нет"},
                    {"success.title", "Успех"},
                    {"script.title", "Выполнение скрипта"},
                    {"script.error", "Ошибка скрипта"},
                    {"login.title", "Авторизация"},
                    {"login.username", "Имя пользователя"},
                    {"login.password", "Пароль"},
                    {"login.button", "Войти"},
                    {"login.error", "Ошибка входа"},
                    {"login.error.empty", "Заполните все поля"},
                    {"register.button", "Регистрация"},
                    {"register.error", "Ошибка регистрации"},
                    {"language.russian", "Русский"},
                    {"language.portuguese", "Португальский"},
                    {"language.polish", "Польский"},
                    {"language.englishNZ", "Английский (NZ)"},
                    {"table.header.id", "ID"},
                    {"table.header.name", "Название"},
                    {"table.header.coordinates", "Координаты"},
                    {"table.header.creationDate", "Дата создания"},
                    {"table.header.price", "Цена"},
                    {"table.header.partNumber", "Номер детали"},
                    {"table.header.manufactureCost", "Стоимость производства"},
                    {"table.header.unit", "Единица измерения"},
                    {"table.header.manufacturer", "Производитель"},
                    {"table.header.creator", "Создатель"},
                    {"filter.title", "Фильтрация продуктов"},
                    {"filter.field", "Поле:"},
                    {"filter.operator", "Оператор:"},
                    {"filter.value", "Значение:"},
                    {"filter.apply", "Применить"},
                    {"filter.clear", "Очистить"},
                    {"filter.error", "Ошибка фильтра: "},
                    {"filter.field.name", "Название"},
                    {"filter.field.price", "Цена"},
                    {"filter.field.manufactureCost", "Стоимость производства"},
                    {"filter.field.partNumber", "Номер детали"},
                    {"filter.field.creatorId", "ID создателя"},
                    {"filter.operator.equals", "Равно"},
                    {"filter.operator.contains", "Содержит"},
                    {"filter.operator.greater", "Больше"},
                    {"filter.operator.less", "Меньше"},
                    {"label.name", "Название"},
                    {"label.coordinates.x", "Координата X"},
                    {"label.coordinates.y", "Координата Y"},
                    {"label.price", "Цена"},
                    {"label.partNumber", "Номер детали"},
                    {"label.manufactureCost", "Стоимость производства"},
                    {"label.unit_of_measure:", "Единица измерения:"},
                    {"product.add.title", "Добавление продукта"},
                    {"product.edit.title", "Редактирование продукта"},

                    // Новые ключи
                    {"label.manufacturer.section", "Производитель (Опционально)"},
                    {"label.manufacturer.name", "Название производителя"},
                    {"label.manufacturer.fullName", "Полное название"},
                    {"label.manufacturer.employeesCount", "Количество сотрудников"},
                    {"label.manufacturer.type", "Тип производителя"},
                    {"error.coordinates.x", "Координата X должна быть больше -349"},
                    {"error.price.positive", "Цена должна быть положительной"},
                    {"error.manufacturer.name.required", "Название производителя обязательно при заполнении других полей"},
                    {"error.manufacturer.fullName.required", "Полное название обязательно при заполнении других полей"},
                    {"error.manufacturer.employeesCount.required", "Количество сотрудников обязательно при заполнении других полей"},
                    {"error.manufacturer.employeesCount.positive", "Количество сотрудников должно быть положительным"},
                    {"error.number.format", "Неверный числовой формат"},
                    {"error.general", "Ошибка: "},
                    {"sort.field.id", "ID"},
                    {"sort.field.name", "Название"},
                    {"sort.field.price", "Цена"},
                    {"sort.field.creationDate", "Дата создания"},
                    {"button.edit", "Редактировать"},
                    {"button.close", "Закрыть"},
                    {"sort.field.manufactureCost", "Стоимость производства"},
                    {"operation.register.success", "Регистрация завершена успешно"},
                    {"operation.login.success", "Вход выполнен успешно"},
                    {"dialog.filter.title", "Фильтрация продуктов"},
                    {"dialog.sort.title", "Сортировка продуктов"},
                    {"dialog.script.title", "Выполнение скрипта"},
                    {"dialog.product.title", "Редактирование продукта"},
                    {"sort.title", "Сортировка продуктов"},
                    {"sort.field", "Поле сортировки:"},
                    {"sort.order", "Порядок:"},
                    {"sort.ascending", "По возрастанию"},
                    {"sort.descending", "По убыванию"},
                    {"sort.apply", "Применить"},
                    {"operation.success", "Операция выполнена успешно"},
                    {"operation.add.success", "Продукт успешно добавлен"},
                    {"operation.update.success", "Продукт успешно обновлен"},
                    {"operation.remove.success", "Продукт успешно удален"},
                    {"operation.error", "Ошибка при выполнении операции"}
            };
        }
    }

    private static class PortugueseBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                    {"application.title", "Aplicação Cliente"},
                    {"menu.file", "Arquivo"},
                    {"menu.exit", "Sair"},
                    {"menu.view", "Visualizar"},
                    {"menu.language", "Idioma"},
                    {"menu.commands", "Comandos"},
                    {"button.add", "Adicionar"},
                    {"button.update", "Atualizar"},
                    {"button.remove", "Remover"},
                    {"button.filter", "Filtrar"},
                    {"button.clear", "Limpar"},
                    {"button.execute", "Executar"},
                    {"button.save", "Salvar"},
                    {"button.cancel", "Cancelar"},
                    {"button.sort", "Ordenar"},
                    {"welcome", "Bem-vindo, {0}"},
                    {"status.connected", "Conectado"},
                    {"status.lastUpdate", "Última atualização: "},
                    {"status.error", "Erro: "},
                    {"error.title", "Erro"},
                    {"error.selectProduct", "Selecione um produto"},
                    {"error.editOtherUser", "Você só pode editar seus próprios produtos"},
                    {"error.deleteOtherUser", "Você só pode remover seus próprios produtos"},
                    {"confirm.title", "Confirmação"},
                    {"confirm.delete", "Tem certeza que deseja remover este produto?"},
                    {"confirm.clear", "Tem certeza que deseja limpar todos os produtos?"},
                    {"option.yes", "Sim"},
                    {"option.no", "Não"},
                    {"success.title", "Sucesso"},
                    {"script.title", "Execução de Script"},
                    {"script.error", "Erro de Script"},
                    {"login.title", "Autenticação"},
                    {"login.username", "Nome de usuário"},
                    {"login.password", "Senha"},
                    {"login.button", "Entrar"},
                    {"login.error", "Erro de Login"},
                    {"login.error.empty", "Preencha todos os campos"},
                    {"register.button", "Registrar"},
                    {"register.error", "Erro de Registro"},
                    {"language.russian", "Russo"},
                    {"language.portuguese", "Português"},
                    {"language.polish", "Polonês"},
                    {"language.englishNZ", "Inglês (NZ)"},
                    {"table.header.id", "ID"},
                    {"table.header.name", "Nome"},
                    {"table.header.coordinates", "Coordenadas"},
                    {"table.header.creationDate", "Data de Criação"},
                    {"table.header.price", "Preço"},
                    {"table.header.partNumber", "Número da Peça"},
                    {"table.header.manufactureCost", "Custo de Fabricação"},
                    {"table.header.unit", "Unidade de Medida"},
                    {"table.header.manufacturer", "Fabricante"},
                    {"table.header.creator", "Criador"},
                    {"filter.title", "Filtragem de Produtos"},
                    {"filter.field", "Campo:"},
                    {"filter.operator", "Operador:"},
                    {"filter.value", "Valor:"},
                    {"filter.apply", "Aplicar"},
                    {"filter.clear", "Limpar"},
                    {"filter.error", "Erro de filtro: "},
                    {"filter.field.name", "Nome"},
                    {"filter.field.price", "Preço"},
                    {"filter.field.manufactureCost", "Custo de Fabricação"},
                    {"filter.field.partNumber", "Número da Peça"},
                    {"filter.field.creatorId", "ID do Criador"},
                    {"filter.operator.equals", "Igual"},
                    {"filter.operator.contains", "Contém"},
                    {"filter.operator.greater", "Maior"},
                    {"filter.operator.less", "Menor"},
                    {"label.name", "Nome"},
                    {"label.coordinates.x", "Coordenada X"},
                    {"label.coordinates.y", "Coordenada Y"},
                    {"label.price", "Preço"},
                    {"label.partNumber", "Número da Peça"},
                    {"label.manufactureCost", "Custo de Fabricação"},
                    {"label.unit_of_measure:", "Unidade de Medida:"},
                    {"product.add.title", "Adicionar Produto"},
                    {"product.edit.title", "Editar Produto"},
                    {"label.manufacturer.section", "Fabricante (Opcional)"},
                    {"label.manufacturer.name", "Nome do fabricante"},
                    {"label.manufacturer.fullName", "Nome completo"},
                    {"label.manufacturer.employeesCount", "Número de funcionários"},
                    {"label.manufacturer.type", "Tipo de fabricante"},
                    {"error.coordinates.x", "Coordenada X deve ser maior que -349"},
                    {"error.price.positive", "Preço deve ser positivo"},
                    {"error.manufacturer.name.required", "Nome do fabricante é obrigatório quando outros campos são preenchidos"},
                    {"error.manufacturer.fullName.required", "Nome completo é obrigatório quando outros campos são preenchidos"},
                    {"error.manufacturer.employeesCount.required", "Número de funcionários é obrigatório quando outros campos são preenchidos"},
                    {"error.manufacturer.employeesCount.positive", "Número de funcionários deve ser positivo"},
                    {"error.number.format", "Formato numérico inválido"},
                    {"error.general", "Erro: "},
                    {"sort.field.id", "ID"},
                    {"sort.field.name", "Nome"},
                    {"sort.field.price", "Preço"},
                    {"button.edit", "Redua"},
                    {"button.close", "Noe"},
                    {"sort.field.creationDate", "Data de Criação"},
                    {"sort.field.manufactureCost", "Custo de Fabricação"},
                    {"operation.register.success", "Registro concluído com sucesso"},
                    {"operation.login.success", "Login realizado com sucesso"},
                    {"dialog.filter.title", "Filtragem de Produtos"},
                    {"dialog.sort.title", "Ordenação de Produtos"},
                    {"dialog.script.title", "Execução de Script"},
                    {"dialog.product.title", "Edição de Produto"},
                    {"sort.title", "Ordenação de Produtos"},
                    {"sort.field", "Campo de ordenação:"},
                    {"sort.order", "Ordem:"},
                    {"sort.ascending", "Crescente"},
                    {"sort.descending", "Decrescente"},
                    {"sort.apply", "Aplicar"},
                    {"operation.success", "Operação concluída com sucesso"},
                    {"operation.add.success", "Produto adicionado com sucesso"},
                    {"operation.update.success", "Produto atualizado com sucesso"},
                    {"operation.remove.success", "Produto removido com sucesso"},
                    {"operation.error", "Erro ao executar a operação"}
            };
        }
    }

    private static class PolishBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                    {"application.title", "Aplikacja Kliencka"},
                    {"menu.file", "Plik"},
                    {"menu.exit", "Wyjście"},
                    {"menu.view", "Widok"},
                    {"menu.language", "Język"},
                    {"menu.commands", "Polecenia"},
                    {"button.add", "Dodaj"},
                    {"button.update", "Aktualizuj"},
                    {"button.remove", "Usuń"},
                    {"button.filter", "Filtruj"},
                    {"button.clear", "Wyczyść"},
                    {"button.execute", "Wykonaj"},
                    {"button.save", "Zapisz"},
                    {"button.cancel", "Anuluj"},
                    {"button.sort", "Sortuj"},
                    {"welcome", "Witaj, {0}"},
                    {"status.connected", "Połączono"},
                    {"status.lastUpdate", "Ostatnia aktualizacja: "},
                    {"status.error", "Błąd: "},
                    {"error.title", "Błąd"},
                    {"error.selectProduct", "Wybierz produkt"},
                    {"error.editOtherUser", "Możesz edytować tylko swoje produkty"},
                    {"error.deleteOtherUser", "Możesz usuwać tylko swoje produkty"},
                    {"confirm.title", "Potwierdzenie"},
                    {"confirm.delete", "Czy na pewno chcesz usunąć ten produkt?"},
                    {"confirm.clear", "Czy na pewno chcesz wyczyścić wszystkie produkty?"},
                    {"option.yes", "Tak"},
                    {"option.no", "Nie"},
                    {"success.title", "Sukces"},
                    {"script.title", "Wykonywanie Skryptu"},
                    {"script.error", "Błąd Skryptu"},
                    {"login.title", "Uwierzytelnianie"},
                    {"login.username", "Nazwa użytkownika"},
                    {"login.password", "Hasło"},
                    {"login.button", "Zaloguj"},
                    {"login.error", "Błąd Logowania"},
                    {"login.error.empty", "Wypełnij wszystkie pola"},
                    {"register.button", "Rejestracja"},
                    {"register.error", "Błąd Rejestracji"},
                    {"language.russian", "Rosyjski"},
                    {"language.portuguese", "Portugalski"},
                    {"language.polish", "Polski"},
                    {"language.englishNZ", "Angielski (NZ)"},
                    {"table.header.id", "ID"},
                    {"table.header.name", "Nazwa"},
                    {"button.edit", "Redoctova"},
                    {"button.close", "Konec"},
                    {"table.header.coordinates", "Współrzędne"},
                    {"table.header.creationDate", "Data utworzenia"},
                    {"table.header.price", "Cena"},
                    {"table.header.partNumber", "Numer części"},
                    {"table.header.manufactureCost", "Koszt produkcji"},
                    {"table.header.unit", "Jednostka miary"},
                    {"table.header.manufacturer", "Producent"},
                    {"table.header.creator", "Twórca"},
                    {"filter.title", "Filtrowanie Produktów"},
                    {"filter.field", "Pole:"},
                    {"filter.operator", "Operator:"},
                    {"filter.value", "Wartość:"},
                    {"filter.apply", "Zastosuj"},
                    {"filter.clear", "Wyczyść"},
                    {"filter.error", "Błąd filtru: "},
                    {"filter.field.name", "Nazwa"},
                    {"filter.field.price", "Cena"},
                    {"filter.field.manufactureCost", "Koszt produkcji"},
                    {"filter.field.partNumber", "Numer części"},
                    {"filter.field.creatorId", "ID Twórcy"},
                    {"filter.operator.equals", "Równe"},
                    {"filter.operator.contains", "Zawiera"},
                    {"filter.operator.greater", "Większe"},
                    {"filter.operator.less", "Mniejsze"},
                    {"label.name", "Nazwa"},
                    {"label.coordinates.x", "Współrzędna X"},
                    {"label.coordinates.y", "Współrzędna Y"},
                    {"label.price", "Cena"},
                    {"label.partNumber", "Numer części"},
                    {"label.manufactureCost", "Koszt produkcji"},
                    {"label.unit_of_measure:", "Jednostka miary:"},
                    {"product.add.title", "Dodawanie Produktu"},
                    {"product.edit.title", "Edycja Produktu"},
                    {"label.manufacturer.section", "Producent (Opcjonalnie)"},
                    {"label.manufacturer.name", "Nazwa producenta"},
                    {"label.manufacturer.fullName", "Pełna nazwa"},
                    {"label.manufacturer.employeesCount", "Liczba pracowników"},
                    {"label.manufacturer.type", "Typ producenta"},
                    {"error.coordinates.x", "Współrzędna X musi być większa niż -349"},
                    {"error.price.positive", "Cena musi być dodatnia"},
                    {"error.manufacturer.name.required", "Nazwa producenta jest wymagana przy wypełnianiu innych pól"},
                    {"error.manufacturer.fullName.required", "Pełna nazwa jest wymagana przy wypełnianiu innych pól"},
                    {"error.manufacturer.employeesCount.required", "Liczba pracowników jest wymagana przy wypełnianiu innych pól"},
                    {"error.manufacturer.employeesCount.positive", "Liczba pracowników musi być dodatnia"},
                    {"error.number.format", "Nieprawidłowy format liczbowy"},
                    {"error.general", "Błąd: "},
                    {"sort.field.id", "ID"},
                    {"sort.field.name", "Nazwa"},
                    {"sort.field.price", "Cena"},
                    {"sort.field.creationDate", "Data utworzenia"},
                    {"sort.field.manufactureCost", "Koszt produkcji"},
                    {"operation.register.success", "Rejestracja zakończona pomyślnie"},
                    {"operation.login.success", "Logowanie zakończone pomyślnie"},
                    {"dialog.filter.title", "Filtrowanie Produktów"},
                    {"dialog.sort.title", "Sortowanie Produktów"},
                    {"dialog.script.title", "Wykonywanie Skryptu"},
                    {"dialog.product.title", "Edycja Produktu"},
                    {"sort.title", "Sortowanie Produktów"},
                    {"sort.field", "Pole sortowania:"},
                    {"sort.order", "Kolejność:"},
                    {"sort.ascending", "Rosnąco"},
                    {"sort.descending", "Malejąco"},
                    {"sort.apply", "Zastosuj"},
                    {"operation.success", "Operacja zakończona pomyślnie"},
                    {"operation.add.success", "Produkt został pomyślnie dodany"},
                    {"operation.update.success", "Produkt został pomyślnie zaktualizowany"},
                    {"operation.remove.success", "Produkt został pomyślnie usunięty"},
                    {"operation.error", "Błąd podczas wykonywania operacji"}

            };
        }
    }

    private static class EnglishNZBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new Object[][] {
                    {"application.title", "Client Application"},
                    {"menu.file", "File"},
                    {"menu.exit", "Exit"},
                    {"menu.view", "View"},
                    {"menu.language", "Language"},
                    {"menu.commands", "Commands"},
                    {"button.add", "Add"},
                    {"button.update", "Update"},
                    {"button.remove", "Remove"},
                    {"button.filter", "Filter"},
                    {"button.edit", "Refresh"},
                    {"button.close", "Close"},
                    {"button.clear", "Clear"},
                    {"button.execute", "Execute"},
                    {"button.save", "Save"},
                    {"button.cancel", "Cancel"},
                    {"button.sort", "Sort"},
                    {"welcome", "Welcome, {0}"},
                    {"status.connected", "Connected"},
                    {"status.lastUpdate", "Last update: "},
                    {"status.error", "Error: "},
                    {"error.title", "Error"},
                    {"error.selectProduct", "Select a product"},
                    {"error.editOtherUser", "You can only edit your own products"},
                    {"error.deleteOtherUser", "You can only remove your own products"},
                    {"confirm.title", "Confirmation"},
                    {"confirm.delete", "Are you sure you want to remove this product?"},
                    {"confirm.clear", "Are you sure you want to clear all products?"},
                    {"option.yes", "Yes"},
                    {"option.no", "No"},
                    {"success.title", "Success"},
                    {"script.title", "Script Execution"},
                    {"script.error", "Script Error"},
                    {"login.title", "Authentication"},
                    {"login.username", "Username"},
                    {"login.password", "Password"},
                    {"login.button", "Login"},
                    {"login.error", "Login Error"},
                    {"login.error.empty", "Please fill all fields"},
                    {"register.button", "Register"},
                    {"register.error", "Registration Error"},
                    {"language.russian", "Russian"},
                    {"language.portuguese", "Portuguese"},
                    {"language.polish", "Polish"},
                    {"language.englishNZ", "English (NZ)"},
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
                    {"filter.title", "Product Filtering"},
                    {"filter.field", "Field:"},
                    {"filter.operator", "Operator:"},
                    {"filter.value", "Value:"},
                    {"filter.apply", "Apply"},
                    {"filter.clear", "Clear"},
                    {"filter.error", "Filter error: "},
                    {"filter.field.name", "Name"},
                    {"filter.field.price", "Price"},
                    {"filter.field.manufactureCost", "Manufacture Cost"},
                    {"filter.field.partNumber", "Part Number"},
                    {"filter.field.creatorId", "Creator ID"},
                    {"filter.operator.equals", "Equals"},
                    {"filter.operator.contains", "Contains"},
                    {"filter.operator.greater", "Greater"},
                    {"filter.operator.less", "Less"},
                    {"label.name", "Name"},
                    {"label.coordinates.x", "Coordinate X"},
                    {"label.coordinates.y", "Coordinate Y"},
                    {"label.price", "Price"},
                    {"label.partNumber", "Part Number"},
                    {"label.manufactureCost", "Manufacture Cost"},
                    {"label.unit_of_measure:", "Unit of Measure:"},
                    {"product.add.title", "Add Product"},
                    {"product.edit.title", "Edit Product"},
                    {"label.manufacturer.section", "Manufacturer (Optional)"},
                    {"label.manufacturer.name", "Manufacturer Name"},
                    {"label.manufacturer.fullName", "Full Name"},
                    {"label.manufacturer.employeesCount", "Employees Count"},
                    {"label.manufacturer.type", "Manufacturer Type"},
                    {"error.coordinates.x", "X coordinate must be greater than -349"},
                    {"error.price.positive", "Price must be positive"},
                    {"error.manufacturer.name.required", "Manufacturer name is required when other fields are filled"},
                    {"error.manufacturer.fullName.required", "Full name is required when other fields are filled"},
                    {"error.manufacturer.employeesCount.required", "Employees count is required when other fields are filled"},
                    {"error.manufacturer.employeesCount.positive", "Employees count must be positive"},
                    {"error.number.format", "Invalid number format"},
                    {"error.general", "Error: "},
                    {"sort.field.id", "ID"},
                    {"sort.field.name", "Name"},
                    {"sort.field.price", "Price"},
                    {"sort.field.creationDate", "Creation Date"},
                    {"sort.field.manufactureCost", "Manufacture Cost"},
                    {"operation.register.success", "Registration completed successfully"},
                    {"operation.login.success", "Login completed successfully"},
                    {"dialog.filter.title", "Product Filtering"},
                    {"dialog.sort.title", "Product Sorting"},
                    {"dialog.script.title", "Script Execution"},
                    {"dialog.product.title", "Product Editing"},
                    {"sort.title", "Product Sorting"},
                    {"sort.field", "Sort field:"},
                    {"sort.order", "Order:"},
                    {"sort.ascending", "Ascending"},
                    {"sort.descending", "Descending"},
                    {"sort.apply", "Apply"},
                    {"operation.success", "Operation completed successfully"},
                    {"operation.add.success", "Product successfully added"},
                    {"operation.update.success", "Product successfully updated"},
                    {"operation.remove.success", "Product successfully removed"},
                    {"operation.error", "Error performing operation"}
            };
        }
    }
}