"use strict";

const host = window.location.hostname || "localhost";
const userApi = `${window.location.protocol}//${host}:8081`;
const debtApi = `${window.location.protocol}//${host}:8082`;
const sessionKey = "financeTrackerUser";
const dashboardRefreshMs = 15000;
let dashboardRefreshTimer = null;
let dashboardRefreshInProgress = false;

const unsafeSqlPattern = /(;|--|\/\*|\*\/|\bunion\s+select\b|\bselect\s+.+\s+from\b|\binsert\s+into\b|\bdelete\s+from\b|\bdrop\s+table\b|\balter\s+table\b|\btruncate\s+table\b|\bupdate\s+.+\s+set\b|\bor\s+1\s*=\s*1\b|\band\s+1\s*=\s*1\b)/i;

const state = {
    user: null,
    token: null,
    incomes: [],
    debts: []
};

const elements = {
    authView: document.getElementById("authView"),
    dashboardView: document.getElementById("dashboardView"),
    logoutButton: document.getElementById("logoutButton"),
    messageBar: document.getElementById("messageBar"),
    loginForm: document.getElementById("loginForm"),
    registerForm: document.getElementById("registerForm"),
    passwordReminderForm: document.getElementById("passwordReminderForm"),
    passwordResetForm: document.getElementById("passwordResetForm"),
    resetTokenBox: document.getElementById("resetTokenBox"),
    accountForm: document.getElementById("accountForm"),
    incomeForm: document.getElementById("incomeForm"),
    debtForm: document.getElementById("debtForm"),
    deleteAccountButton: document.getElementById("deleteAccountButton"),
    incomeList: document.getElementById("incomeList"),
    debtList: document.getElementById("debtList"),
    lastRefreshTime: document.getElementById("lastRefreshTime")
};

async function request(url, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(state.token ? { Authorization: `Bearer ${state.token}` } : {}),
        ...(options.headers || {})
    };

    const response = await fetch(url, { ...options, headers });

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    const body = text ? JSON.parse(text) : null;
    if (!response.ok) {
        throw new Error(body?.message || `Request failed with status ${response.status}`);
    }
    return body;
}

function showMessage(message, type = "success") {
    elements.messageBar.textContent = message;
    elements.messageBar.className = `message ${type}`;
    window.clearTimeout(showMessage.timeout);
    showMessage.timeout = window.setTimeout(() => {
        elements.messageBar.className = "message hidden";
    }, 5000);
}

function formDataToObject(form) {
    return Object.fromEntries(new FormData(form).entries());
}

function assertSafeText(fieldName, value) {
    if (typeof value === "string" && unsafeSqlPattern.test(value)) {
        throw new Error(`${fieldName} contains unsafe SQL-like input.`);
    }
}

function assertSafeFields(data, fields) {
    fields.forEach(field => assertSafeText(field, data[field]));
}

function currency(value) {
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD"
    }).format(Number(value || 0));
}

function titleCase(value) {
    return String(value || "")
        .toLowerCase()
        .split("_")
        .map(part => part.charAt(0).toUpperCase() + part.slice(1))
        .join(" ");
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function saveSession(login) {
    localStorage.setItem(sessionKey, JSON.stringify({ id: login.id, token: login.token }));
}

function clearSession() {
    stopDashboardAutoRefresh();
    localStorage.removeItem(sessionKey);
    state.user = null;
    state.token = null;
    state.incomes = [];
    state.debts = [];
}

function showAuth() {
    elements.authView.classList.remove("hidden");
    elements.dashboardView.classList.add("hidden");
    elements.logoutButton.classList.add("hidden");
}

function showDashboard() {
    elements.authView.classList.add("hidden");
    elements.dashboardView.classList.remove("hidden");
    elements.logoutButton.classList.remove("hidden");
    startDashboardAutoRefresh();
}

function startDashboardAutoRefresh() {
    stopDashboardAutoRefresh();
    dashboardRefreshTimer = window.setInterval(async () => {
        if (!state.user || elements.dashboardView.classList.contains("hidden")) {
            return;
        }
        try {
            await loadDashboard();
        } catch {
            // Keep the dashboard usable if a background refresh fails once.
        }
    }, dashboardRefreshMs);
}

function stopDashboardAutoRefresh() {
    if (dashboardRefreshTimer) {
        window.clearInterval(dashboardRefreshTimer);
        dashboardRefreshTimer = null;
    }
}

function markLastRefresh() {
    if (elements.lastRefreshTime) {
        elements.lastRefreshTime.textContent = `Last refreshed ${new Date().toLocaleTimeString()}.`;
    }
}

function populateAccountForm() {
    const profile = state.user.profile || {};
    document.getElementById("welcomeName").textContent = `${profile.firstName || ""} ${profile.lastName || ""}`.trim() || state.user.username;
    document.getElementById("accountFirstName").value = profile.firstName || "";
    document.getElementById("accountLastName").value = profile.lastName || "";
    document.getElementById("accountUsername").value = state.user.username || "";
    document.getElementById("accountEmail").value = state.user.email || "";
    document.getElementById("accountPassword").value = "";
    document.getElementById("accountActive").checked = Boolean(state.user.active);
}

function renderSummary() {
    const monthlyIncome = state.incomes
        .filter(item => item.active)
        .reduce((sum, item) => sum + Number(item.monthlyGrossAmount || 0), 0);
    const yearlyIncome = state.incomes
        .filter(item => item.active)
        .reduce((sum, item) => sum + Number(item.yearlyGrossAmount || 0), 0);
    const currentDebt = state.debts.reduce((sum, item) => sum + Number(item.currentBalance || 0), 0);
    const minimumPayments = state.debts
        .filter(item => item.status === "ACTIVE")
        .reduce((sum, item) => sum + Number(item.minimumPayment || 0), 0);

    document.getElementById("monthlyIncomeTotal").textContent = currency(monthlyIncome);
    document.getElementById("yearlyIncomeTotal").textContent = currency(yearlyIncome);
    document.getElementById("debtTotal").textContent = currency(currentDebt);
    document.getElementById("minimumPaymentTotal").textContent = currency(minimumPayments);
}

function renderIncomes() {
    if (state.incomes.length === 0) {
        elements.incomeList.innerHTML = '<div class="empty-state">No income records yet.</div>';
        return;
    }

    elements.incomeList.innerHTML = state.incomes.map(item => `
        <article class="record-item">
            <div>
                <h3 class="record-title">${escapeHtml(item.source)}</h3>
                <p class="record-meta">
                    ${currency(item.grossAmount)} ${titleCase(item.frequency)} gross<br>
                    ${currency(item.monthlyGrossAmount)} monthly, ${currency(item.yearlyGrossAmount)} yearly
                </p>
            </div>
            <div class="record-actions">
                <button class="button button-danger button-small" data-delete-income="${item.id}" type="button">Remove</button>
            </div>
        </article>
    `).join("");
}

function renderDebts() {
    if (state.debts.length === 0) {
        elements.debtList.innerHTML = '<div class="empty-state">No debt accounts yet.</div>';
        return;
    }

    elements.debtList.innerHTML = state.debts.map(item => `
        <article class="record-item">
            <div>
                <h3 class="record-title">${escapeHtml(item.debtName)}</h3>
                <p class="record-meta">
                    ${titleCase(item.debtType)} · ${item.interestRate}% APR · ${titleCase(item.status)}<br>
                    Balance ${currency(item.currentBalance)} · Minimum ${currency(item.minimumPayment)}
                </p>
            </div>
            <div class="record-actions">
                ${item.status === "ACTIVE" ? `
                    <input class="payment-input" data-payment-input="${item.id}" type="number" min="0.01" max="${item.currentBalance}" step="0.01" placeholder="Payment">
                    <button class="button button-small" data-pay-debt="${item.id}" type="button">Pay</button>
                ` : ""}
                <button class="button button-danger button-small" data-delete-debt="${item.id}" type="button">Remove</button>
            </div>
        </article>
    `).join("");
}

async function loadDashboard() {
    if (!state.user || dashboardRefreshInProgress) {
        return;
    }

    dashboardRefreshInProgress = true;
    try {
        const [user, incomes, debts] = await Promise.all([
            request(`${userApi}/users/${state.user.id}`),
            request(`${userApi}/incomes/user/${state.user.id}`),
            request(`${debtApi}/debts/user/${state.user.id}`)
        ]);
        state.user = user;
        state.incomes = incomes;
        state.debts = debts;
        populateAccountForm();
        renderIncomes();
        renderDebts();
        renderSummary();
        markLastRefresh();
        showDashboard();
    } finally {
        dashboardRefreshInProgress = false;
    }
}

async function refreshDashboard(message) {
    await loadDashboard();
    if (message) {
        showMessage(message);
    }
}

window.addEventListener("focus", async () => {
    if (state.user && !elements.dashboardView.classList.contains("hidden")) {
        try {
            await loadDashboard();
        } catch {
            // Ignore focus refresh errors and keep the current view.
        }
    }
});

async function completeLogin(login, message) {
    state.token = login.token;
    state.user = await request(`${userApi}/users/${login.id}`);
    saveSession(login);
    await loadDashboard();
    showMessage(message);
}

elements.loginForm.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const data = formDataToObject(event.currentTarget);
        assertSafeFields(data, ["username"]);
        const login = await request(`${userApi}/auth/login`, {
            method: "POST",
            body: JSON.stringify(data)
        });
        await completeLogin(login, "Signed in successfully.");
    } catch (error) {
        showMessage(error.message, "error");
    }
});

elements.registerForm.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const data = formDataToObject(event.currentTarget);
        assertSafeFields(data, ["firstName", "lastName", "username", "email"]);
        const login = await request(`${userApi}/auth/register`, {
            method: "POST",
            body: JSON.stringify({ ...data, active: true })
        });
        event.currentTarget.reset();
        await completeLogin(login, "Account created and signed in.");
    } catch (error) {
        showMessage(error.message, "error");
    }
});

elements.passwordReminderForm.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const data = formDataToObject(event.currentTarget);
        assertSafeFields(data, ["email"]);
        const response = await request(`${userApi}/auth/password-reminder`, {
            method: "POST",
            body: JSON.stringify(data)
        });
        elements.resetTokenBox.textContent = response.resetToken;
        document.getElementById("resetToken").value = response.resetToken;
        showMessage("Reset token created. Use it in the reset form.");
    } catch (error) {
        showMessage(error.message, "error");
    }
});

elements.passwordResetForm.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const data = formDataToObject(event.currentTarget);
        const login = await request(`${userApi}/auth/password-reset`, {
            method: "POST",
            body: JSON.stringify(data)
        });
        event.currentTarget.reset();
        elements.resetTokenBox.textContent = "";
        await completeLogin(login, "Password reset and signed in.");
    } catch (error) {
        showMessage(error.message, "error");
    }
});

elements.accountForm.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const data = formDataToObject(event.currentTarget);
        assertSafeFields(data, ["firstName", "lastName", "username", "email"]);
        const payload = {
            username: data.username,
            email: data.email,
            firstName: data.firstName,
            lastName: data.lastName,
            password: data.password || null,
            active: document.getElementById("accountActive").checked
        };
        state.user = await request(`${userApi}/users/${state.user.id}`, {
            method: "PUT",
            body: JSON.stringify(payload)
        });
        await refreshDashboard("Account updated.");
    } catch (error) {
        showMessage(error.message, "error");
    }
});

elements.incomeForm.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const data = formDataToObject(event.currentTarget);
        assertSafeFields(data, ["source"]);
        await request(`${userApi}/incomes`, {
            method: "POST",
            body: JSON.stringify({
                userId: state.user.id,
                source: data.source,
                grossAmount: Number(data.grossAmount),
                frequency: data.frequency,
                effectiveDate: data.effectiveDate || null,
                active: true
            })
        });
        event.currentTarget.reset();
        await refreshDashboard("Income added.");
    } catch (error) {
        showMessage(error.message, "error");
    }
});

elements.debtForm.addEventListener("submit", async event => {
    event.preventDefault();
    try {
        const data = formDataToObject(event.currentTarget);
        assertSafeFields(data, ["debtName"]);
        const balance = Number(data.balance);
        await request(`${debtApi}/debts`, {
            method: "POST",
            body: JSON.stringify({
                userId: state.user.id,
                debtName: data.debtName,
                debtType: data.debtType,
                originalBalance: balance,
                currentBalance: balance,
                interestRate: Number(data.interestRate || 0),
                minimumPayment: Number(data.minimumPayment || 0),
                dueDate: data.dueDate || null,
                status: "ACTIVE"
            })
        });
        event.currentTarget.reset();
        await refreshDashboard("Debt added.");
    } catch (error) {
        showMessage(error.message, "error");
    }
});

elements.incomeList.addEventListener("click", async event => {
    const button = event.target.closest("[data-delete-income]");
    if (!button) return;
    if (!window.confirm("Remove this income record?")) return;
    try {
        await request(`${userApi}/incomes/${button.dataset.deleteIncome}`, { method: "DELETE" });
        await refreshDashboard("Income removed.");
    } catch (error) {
        showMessage(error.message, "error");
    }
});

elements.debtList.addEventListener("click", async event => {
    const deleteButton = event.target.closest("[data-delete-debt]");
    const payButton = event.target.closest("[data-pay-debt]");

    try {
        if (deleteButton) {
            if (!window.confirm("Remove this debt account?")) return;
            await request(`${debtApi}/debts/${deleteButton.dataset.deleteDebt}`, { method: "DELETE" });
            showMessage("Debt removed.");
        } else if (payButton) {
            const id = payButton.dataset.payDebt;
            const input = document.querySelector(`[data-payment-input="${id}"]`);
            const amount = Number(input.value);
            if (!amount || amount <= 0) {
                throw new Error("Enter a payment amount greater than zero.");
            }
            await request(`${debtApi}/debts/${id}/payments`, {
                method: "POST",
                body: JSON.stringify({ amount, paymentDate: null, note: "Payment from web dashboard" })
            });
            showMessage("Payment recorded.");
        } else {
            return;
        }

        await refreshDashboard();
    } catch (error) {
        showMessage(error.message, "error");
    }
});

elements.deleteAccountButton.addEventListener("click", async () => {
    const confirmed = window.confirm("Delete this account and its visible income and debt records? This cannot be undone.");
    if (!confirmed) return;
    try {
        const debts = await request(`${debtApi}/debts/user/${state.user.id}`);
        await Promise.all(debts.map(debt => request(`${debtApi}/debts/${debt.id}`, { method: "DELETE" })));
        await request(`${userApi}/users/${state.user.id}`, { method: "DELETE" });
        clearSession();
        showAuth();
        showMessage("Account deleted.");
    } catch (error) {
        showMessage(error.message, "error");
    }
});

elements.logoutButton.addEventListener("click", () => {
    clearSession();
    showAuth();
    showMessage("Signed out.");
});

async function restoreSession() {
    const saved = JSON.parse(localStorage.getItem(sessionKey) || "null");
    if (!saved?.id || !saved?.token) {
        showAuth();
        return;
    }

    try {
        state.token = saved.token;
        state.user = await request(`${userApi}/users/${saved.id}`);
        if (!state.user.active) {
            throw new Error("Account is inactive");
        }
        await loadDashboard();
    } catch {
        clearSession();
        showAuth();
    }
}

restoreSession();
