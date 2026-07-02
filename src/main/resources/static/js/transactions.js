/**
 * transactions.js — улучшения формы добавления транзакций.
 *
 *  1. Фильтрация категорий по типу (INCOME / EXPENSE):
 *     Когда пользователь выбирает «Доход» — в дропдауне остаются только
 *     категории с data-type="INCOME", и наоборот. Без JS — все категории видны.
 *
 *  2. Клиентская валидация перед отправкой формы:
 *     Повторяет правила серверной Bean Validation (@NotNull, @Positive, @PastOrPresent).
 *     Показывает ошибки немедленно — без roundtrip на сервер.
 *     Без JS — форма всё равно валидируется на сервере, только ошибки появятся позже.
 */
'use strict';

document.addEventListener('DOMContentLoaded', () => {

    const form           = document.getElementById('transaction-form');
    if (!form) return;   // Скрипт загружен, но формы на странице нет — выходим

    const amountInput    = document.getElementById('amount');
    const typeSelect     = document.getElementById('type');
    const categorySelect = document.getElementById('categoryId');
    const dateInput      = document.getElementById('date');

    // =========================================================
    // 1. Фильтрация категорий по типу транзакции
    // =========================================================

    /**
     * Прячет <option> категорий, чей data-type не совпадает с выбранным типом.
     * Если тип не выбран — показываем все категории.
     */
    function filterCategories() {
        const selectedType = typeSelect.value;

        Array.from(categorySelect.options).forEach(opt => {
            if (!opt.value) {           // «— без категории —» всегда видна
                opt.hidden = false;
                return;
            }
            // data-type проставлен в шаблоне: th:attr="data-type=${cat.type}"
            const match = !selectedType || opt.dataset.type === selectedType;
            opt.hidden = !match;
        });

        // Если текущий выбор стал скрытым — сбрасываем
        const selected = categorySelect.selectedOptions[0];
        if (selected && selected.hidden) {
            categorySelect.value = '';
        }
    }

    typeSelect.addEventListener('change', filterCategories);
    filterCategories(); // применяем при загрузке (важно если форма предзаполнена)

    // =========================================================
    // 2. Вспомогательные функции для ошибок
    // =========================================================

    /**
     * Помечает поле как невалидное и показывает сообщение.
     * Добавляет Bootstrap-класс is-invalid и заполняет .invalid-feedback.
     * (Элемент .invalid-feedback уже есть в шаблоне через th:errors)
     */
    function showError(input, message) {
        input.classList.remove('is-valid');
        input.classList.add('is-invalid');
        const fb = input.nextElementSibling;
        if (fb && fb.classList.contains('invalid-feedback')) {
            fb.textContent = message;
        }
    }

    function clearError(input) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
    }

    // =========================================================
    // 3. Real-time валидация (при вводе)
    // =========================================================

    amountInput.addEventListener('input', () => {
        const v = parseFloat(amountInput.value);
        if (amountInput.value && !isNaN(v) && v > 0) clearError(amountInput);
    });

    typeSelect.addEventListener('change', () => {
        if (typeSelect.value) clearError(typeSelect);
    });

    dateInput.addEventListener('change', () => {
        if (!dateInput.value) return;
        const picked = new Date(dateInput.value);
        const today  = new Date(); today.setHours(23, 59, 59, 999);
        if (picked <= today) clearError(dateInput);
    });

    // =========================================================
    // 4. Валидация при сабмите
    // =========================================================

    form.addEventListener('submit', e => {
        let valid = true;

        // Сумма
        const amount = parseFloat(amountInput.value);
        if (!amountInput.value || isNaN(amount) || amount <= 0) {
            showError(amountInput, 'Сумма должна быть больше нуля');
            valid = false;
        } else {
            clearError(amountInput);
        }

        // Тип
        if (!typeSelect.value) {
            showError(typeSelect, 'Выберите тип транзакции');
            valid = false;
        } else {
            clearError(typeSelect);
        }

        // Дата
        if (!dateInput.value) {
            showError(dateInput, 'Укажите дату');
            valid = false;
        } else {
            const picked = new Date(dateInput.value);
            const today  = new Date(); today.setHours(23, 59, 59, 999);
            if (picked > today) {
                showError(dateInput, 'Дата не может быть в будущем');
                valid = false;
            } else {
                clearError(dateInput);
            }
        }

        // Блокируем отправку только при клиентских ошибках
        if (!valid) {
            e.preventDefault();
            // Скроллим к первому невалидному полю
            form.querySelector('.is-invalid')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    });

});
