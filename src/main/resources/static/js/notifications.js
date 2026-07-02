/**
 * notifications.js — глобальная система уведомлений.
 *
 * Работает в двух режимах:
 *   1. Авто-скрытие серверных флеш-алертов (.alert-dismissible) через 4 секунды.
 *   2. Программные toast-уведомления из JS через window.showToast(message, type).
 *
 * Прогрессивное улучшение: если JS выключен, серверные алерты просто остаются на экране.
 */
'use strict';

document.addEventListener('DOMContentLoaded', () => {

    // --- 1. Авто-скрытие серверных .alert-dismissible ---
    document.querySelectorAll('.alert-dismissible').forEach(alertEl => {
        setTimeout(() => {
            // bootstrap.Alert — API Bootstrap 5 для управления алертом
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alertEl);
            if (bsAlert) bsAlert.close();
        }, 4000);
    });

});

/**
 * Показывает Bootstrap-toast в правом нижнем углу.
 * Используется другими скриптами для AJAX-фидбека.
 *
 * @param {string} message  Текст уведомления
 * @param {'success'|'danger'|'warning'|'info'} type Bootstrap color variant
 */
window.showToast = function (message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const id = 'toast-' + Date.now();
    // Создаём HTML тоста и вставляем в контейнер
    container.insertAdjacentHTML('beforeend', `
        <div id="${id}" class="toast align-items-center text-bg-${type} border-0"
             role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body fw-semibold">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto"
                        data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `);

    const toastEl = document.getElementById(id);
    // Bootstrap Toast API: показать с задержкой 4с, потом удалить из DOM
    const toast = new bootstrap.Toast(toastEl, { delay: 4000 });
    toast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
};
