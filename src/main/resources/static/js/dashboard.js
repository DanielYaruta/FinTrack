/**
 * dashboard.js — AJAX-обновление дашборда без перезагрузки страницы.
 *
 * Что делает:
 *   1. Создаёт Chart.js-график из данных, которые Thymeleaf положил в window.FINTRACK.
 *   2. Каждые 30 секунд опрашивает /api/analytics/metrics → обновляет карточки.
 *   3. Каждые 30 секунд опрашивает /api/analytics/trend  → обновляет график.
 *
 * Прогрессивное улучшение:
 *   - Без JS: карточки и данные рендерятся сервером и не обновляются в реальном времени.
 *   - С JS:   карточки «живые», данные свежие без F5.
 *
 * document.hidden (Page Visibility API) — не опрашиваем сервер, пока вкладка неактивна.
 */
'use strict';

document.addEventListener('DOMContentLoaded', () => {

    // =========================================================
    // 1. Инициализация Chart.js
    // =========================================================

    // window.FINTRACK заполняется Thymeleaf th:inline="javascript" в dashboard.html
    const initialData = window.FINTRACK || {};

    const ctx = document.getElementById('trendChart');
    if (!ctx) return;

    /**
     * Chart.js instance хранится в переменной, чтобы позже обновлять данные через API
     * без пересоздания canvas-элемента.
     */
    const trendChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: initialData.chartMonths || [],
            datasets: [
                {
                    label: 'Доходы',
                    data: initialData.chartIncomes || [],
                    backgroundColor: 'rgba(25, 135, 84, 0.75)',
                    borderRadius: 4
                },
                {
                    label: 'Расходы',
                    data: initialData.chartExpenses || [],
                    backgroundColor: 'rgba(220, 53, 69, 0.75)',
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            plugins: { legend: { position: 'top' } },
            scales: {
                y: {
                    ticks: {
                        callback: v => '₽ ' + v.toLocaleString('ru-RU')
                    }
                }
            }
        }
    });

    // =========================================================
    // 2. Форматирование суммы
    // =========================================================

    function formatMoney(value) {
        //   — узкий неразрывный пробел, стандарт для денежных сумм (₽ 85 000,00)
        return '₽ ' + Number(value).toLocaleString('ru-RU', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    // =========================================================
    // 3. Обновление карточек метрик
    // =========================================================

    /**
     * Получает свежие метрики и обновляет DOM.
     * id-атрибуты (metric-income и т.д.) проставлены в dashboard.html.
     */
    function refreshMetrics() {
        fetch('/api/analytics/metrics')
            .then(r => {
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.json();
            })
            .then(m => {
                const get = id => document.getElementById(id);

                const incomeEl  = get('metric-income');
                const expenseEl = get('metric-expense');
                const balanceEl = get('metric-balance');
                const countEl   = get('metric-count');

                if (incomeEl)  incomeEl.textContent  = formatMoney(m.totalIncome);
                if (expenseEl) expenseEl.textContent = formatMoney(m.totalExpense);

                if (balanceEl) {
                    balanceEl.textContent = formatMoney(m.balance);
                    // Перекрашиваем баланс: зелёный если > 0, красный если < 0
                    balanceEl.className = 'metric-value ' +
                        (Number(m.balance) >= 0 ? 'amount-income' : 'amount-expense');
                }

                if (countEl) countEl.textContent = m.transactionCount;
            })
            .catch(err => console.warn('[FinTrack] metrics refresh error:', err));
    }

    // =========================================================
    // 4. Обновление графика
    // =========================================================

    function refreshChart() {
        fetch('/api/analytics/trend?months=6')
            .then(r => r.json())
            .then(trend => {
                // Обновляем данные существующего Chart.js instance
                trendChart.data.labels              = trend.map(m => m.month);
                trendChart.data.datasets[0].data    = trend.map(m => m.income);
                trendChart.data.datasets[1].data    = trend.map(m => m.expense);
                // 'none' — обновить данные без анимации (выглядит плавнее при polling)
                trendChart.update('none');
            })
            .catch(err => console.warn('[FinTrack] chart refresh error:', err));
    }

    // =========================================================
    // 5. Polling — каждые 30 секунд
    // =========================================================

    setInterval(() => {
        // Page Visibility API: не тратим запросы пока вкладка в фоне
        if (!document.hidden) {
            refreshMetrics();
            refreshChart();
        }
    }, 30_000);

});
