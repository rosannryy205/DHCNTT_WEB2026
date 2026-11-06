/* Order listing and confirmed workflow actions. */
(() => {
  'use strict';

  const page = document.getElementById('ordersPage');
  if (!page) return;

  const orderStatuses = {
    PENDING: { label: 'Chờ xác nhận', tone: 'warning' },
    PROCESSING: { label: 'Đang xử lý', tone: 'blue' },
    SHIPPED: { label: 'Đang giao', tone: 'blue' },
    DELIVERED: { label: 'Đã giao', tone: 'success' },
    CANCELLED: { label: 'Đã hủy', tone: 'neutral' }
  };
  const paymentStatuses = {
    PENDING: { label: 'Chờ thanh toán', tone: 'warning' },
    PAID: { label: 'Đã thanh toán', tone: 'success' },
    FAILED: { label: 'Thanh toán thất bại', tone: 'danger' },
    REFUNDED: { label: 'Đã hoàn tiền', tone: 'neutral' }
  };
  const orders = window.adminOrders || [];
  const nextStatuses = { PENDING: 'PROCESSING', PROCESSING: 'SHIPPED', SHIPPED: 'DELIVERED' };
  const actionLabels = { PROCESSING: 'Duyệt đơn', SHIPPED: 'Bàn giao vận chuyển', DELIVERED: 'Xác nhận đã giao', CANCELLED: 'Hủy đơn' };
  let pendingAction = null;
  let saving = false;
  let detailCode = null;

  function actionButtons(order) {
    const next = ['PENDING', 'PAID'].includes(order.payment) && nextStatuses[order.status];
    const canCancel = order.payment === 'PENDING' && ['PENDING', 'PROCESSING'].includes(order.status);
    if (!next && !canCancel) return '';
    return `<div class="orders-action-buttons">${next ? `<button type="button" class="orders-button orders-button-primary" data-action="${next}" data-code="${escape(order.code)}">${actionLabels[next]}</button>` : ''}${canCancel ? `<button type="button" class="orders-button orders-button-danger" data-action="CANCELLED" data-code="${escape(order.code)}">Hủy đơn</button>` : ''}</div>`;
  }

  const state = { status: 'ALL', payment: 'ALL', search: '', sort: 'newest', page: 1, pageSize: 8 };
  const get = id => document.getElementById(id);
  const currency = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
  const dateFormatter = new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', timeZone: 'Asia/Ho_Chi_Minh' });
  const timeFormatter = new Intl.DateTimeFormat('vi-VN', { hour: '2-digit', minute: '2-digit', hourCycle: 'h23', timeZone: 'Asia/Ho_Chi_Minh' });
  const money = value => currency.format(value);
  const date = value => dateFormatter.format(new Date(value));
  const time = value => timeFormatter.format(new Date(value));
  const normalize = value => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase();
  const escape = value => String(value).replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]);
  const initials = name => escape((name || '').trim().split(/\s+/).slice(-2).map(part => part[0] || '').join(''));
  const badge = status => `<span class="orders-badge orders-badge-${status.tone}"><span class="orders-dot" aria-hidden="true"></span>${status.label}</span>`;
  const iconPaths = {
    product: '<path d="m3 7 9-4 9 4v10l-9 4-9-4zM3 7l9 4 9-4M12 11v10"/>',
    phone: '<rect x="6" y="2" width="12" height="20" rx="3"/><path d="M10 5h4m-3 14h2"/>',
    tablet: '<rect x="3" y="2" width="18" height="20" rx="2"/><path d="M11 19h2"/>',
    laptop: '<path d="M4 16V4h16v12M2 16h20l-2 4H4zM9 17h6"/>',
    audio: '<path d="M5 11V8a7 7 0 0 1 14 0v3M5 10H3v7h4v-7zm14 0h2v7h-4v-7z"/>',
    watch: '<rect x="5" y="6" width="14" height="12" rx="4"/><path d="m8 6 1-4h6l1 4M8 18l1 4h6l1-4m-4-9v3l2 1"/>'
  };
  const productIcon = item => `<span class="orders-product-icon" aria-hidden="true"><svg class="orders-icon" viewBox="0 0 24 24">${iconPaths[item.icon]}</svg></span>`;

  function filteredOrders() {
    const query = normalize(state.search.trim());
    return orders.filter(order =>
      (state.status === 'ALL' || order.status === state.status) &&
      (state.payment === 'ALL' || order.payment === state.payment) &&
      (!query || normalize(`${order.code} ${order.name} ${order.phone} ${order.email}`).includes(query))
    ).sort((a, b) => {
      if (state.sort === 'highest') return b.total - a.total;
      if (state.sort === 'lowest') return a.total - b.total;
      return state.sort === 'oldest' ? a.date.localeCompare(b.date) : b.date.localeCompare(a.date);
    });
  }

  function render() {
    const filtered = filteredOrders();
    const pageCount = Math.max(1, Math.ceil(filtered.length / state.pageSize));
    state.page = Math.min(state.page, pageCount);
    const offset = (state.page - 1) * state.pageSize;
    get('ordersTableBody').innerHTML = filtered.slice(offset, offset + state.pageSize).map(order => {
      const item = order.items[0] || { name: 'Không có sản phẩm', variant: '', quantity: 0, icon: 'product' };
      const extra = order.items.length > 1 ? `+${order.items.length - 1} sản phẩm khác` : `${item.variant} · SL: ${item.quantity}`;
      return `<tr>
        <td><button type="button" class="orders-order-link numeric" data-order="${escape(order.code)}" aria-label="Xem đơn hàng ${escape(order.code)}">#${escape(order.code)}</button><span class="orders-secondary numeric">${date(order.date)} <span aria-hidden="true">·</span> ${time(order.date)}</span></td>
        <td><div class="orders-customer"><span class="orders-customer-avatar" aria-hidden="true">${initials(order.name)}</span><div><span class="orders-customer-name">${escape(order.name)}</span><span class="orders-secondary numeric">${escape(order.phone)}</span></div></div></td>
        <td><div class="orders-product">${productIcon(item)}<div><span class="orders-product-name">${escape(item.name)}</span><span class="orders-secondary">${escape(extra)}</span></div></div></td>
        <td class="orders-align-right orders-total numeric">${money(order.total)}</td>
        <td>${badge(paymentStatuses[order.payment])}</td>
        <td class="orders-col-status">${badge(orderStatuses[order.status])}</td>
        <td class="orders-col-actions">${actionButtons(order) || '<span class="orders-muted">—</span>'}</td>
        <td class="orders-col-detail"><button type="button" class="orders-icon-button" data-order="${escape(order.code)}" aria-label="Xem chi tiết đơn hàng ${escape(order.code)}" title="Xem chi tiết">›</button></td>
      </tr>`;
    }).join('');
    get('ordersEmpty').hidden = filtered.length > 0;
    get('orderCount').textContent = `${filtered.length} đơn hàng`;
    get('pageSummary').textContent = filtered.length ? `Hiển thị ${offset + 1}–${Math.min(offset + state.pageSize, filtered.length)} trong ${filtered.length} đơn hàng` : 'Không có đơn hàng phù hợp';
    get('exportOrders').disabled = filtered.length === 0;
    get('pageButtons').innerHTML = `<button type="button" class="orders-page-button" data-page="${state.page - 1}" aria-label="Trang trước" ${state.page === 1 ? 'disabled' : ''}>‹</button>` +
      Array.from({ length: pageCount }, (_, index) => `<button type="button" class="orders-page-button" data-page="${index + 1}" aria-label="Trang ${index + 1}" ${state.page === index + 1 ? 'aria-current="page"' : ''}>${index + 1}</button>`).join('') +
      `<button type="button" class="orders-page-button" data-page="${state.page + 1}" aria-label="Trang sau" ${state.page === pageCount ? 'disabled' : ''}>›</button>`;
    page.querySelectorAll('[data-status]').forEach(button => {
      const active = button.dataset.status === state.status;
      button.classList.toggle('is-active', active);
      button.setAttribute('aria-pressed', String(active));
    });
    const summaries = [];
    if (state.status !== 'ALL') summaries.push(orderStatuses[state.status].label);
    if (state.payment !== 'ALL') summaries.push(paymentStatuses[state.payment].label);
    if (state.search.trim()) summaries.push(`Từ khóa: “${state.search.trim()}”`);
    get('activeFilters').hidden = summaries.length === 0;
    get('filterSummary').textContent = summaries.join(' · ');
  }

  function timeline(order) {
    if (order.status === 'CANCELLED') {
      return `<li class="is-complete"><span class="orders-timeline-mark">✓</span><div>Đơn hàng được tạo<span class="orders-secondary">${date(order.date)} · ${time(order.date)}</span></div></li><li class="is-cancelled"><span class="orders-timeline-mark">×</span><div>Đơn hàng đã hủy<span class="orders-secondary">${paymentStatuses[order.payment].label}</span></div></li>`;
    }
    const stages = ['Đơn hàng được tạo', 'Xác nhận & chuẩn bị hàng', 'Bàn giao vận chuyển', 'Giao hàng thành công'];
    const current = ['PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED'].indexOf(order.status);
    return stages.map((label, index) => {
      const tone = index < current ? 'is-complete' : index === current ? 'is-current' : 'is-future';
      const description = index === 0 ? `${date(order.date)} · ${time(order.date)}` : index < current || current === 3 ? 'Đã hoàn tất' : index === current ? 'Bước hiện tại' : 'Chưa thực hiện';
      return `<li class="${tone}"><span class="orders-timeline-mark" aria-hidden="true">${index <= current ? '✓' : index + 1}</span><div>${label}<span class="orders-secondary">${description}</span></div></li>`;
    }).join('');
  }

  function openOrder(code) {
    const order = orders.find(item => item.code === code);
    if (!order) return;
    detailCode = code;
    get('detailTitle').textContent = `#${order.code}`;
    get('orderDetailContent').innerHTML = `
      <section class="orders-detail-section"><div class="orders-detail-status">${badge(orderStatuses[order.status])}${badge(paymentStatuses[order.payment])}</div><p class="orders-detail-date">Đặt ngày ${date(order.date)} lúc ${time(order.date)}</p></section>
      <section class="orders-detail-section"><h3>Khách hàng & giao nhận</h3><div class="orders-detail-person"><span class="orders-customer-avatar" aria-hidden="true">${initials(order.name)}</span><div><h4>${escape(order.name)}</h4><p>${escape(order.phone)}<br>${escape(order.email)}</p></div></div><div class="orders-detail-address"><strong>Địa chỉ nhận hàng</strong>${escape(order.address)}</div></section>
      <section class="orders-detail-section"><h3>Sản phẩm <span class="orders-count">${order.items.reduce((sum, item) => sum + item.quantity, 0)}</span></h3>${order.items.map(item => `<div class="orders-line-item">${productIcon(item)}<div><strong>${escape(item.name)}</strong><span class="orders-secondary">${escape(item.variant)}</span><span class="orders-secondary">SKU: ${escape(item.sku)} · SL: ${item.quantity}</span></div><span class="numeric">${money(item.total)}</span></div>`).join('')}</section>
      <section class="orders-detail-section"><h3>Thông tin thanh toán</h3><dl class="orders-costs numeric"><dt>Tạm tính</dt><dd>${money(order.subtotal)}</dd>${order.adjustment ? `<dt>Điều chỉnh tổng tiền</dt><dd>${money(order.adjustment)}</dd>` : ''}<dt class="orders-grand-total">Tổng cộng</dt><dd class="orders-grand-total">${money(order.total)}</dd></dl><p class="orders-payment-method"><span aria-hidden="true">▱</span>${escape(order.method)}</p></section>
      <section class="orders-detail-section"><h3>Tiến trình đơn hàng</h3><ol class="orders-timeline">${timeline(order)}</ol></section>
      <section class="orders-detail-section"><h3>Ghi chú đơn hàng</h3><p class="orders-detail-note">${escape(order.note || 'Không có ghi chú từ khách hàng.')}</p></section>
      ${order.cancellationReason ? `<section class="orders-detail-section"><h3>Lý do hủy đơn</h3><p class="orders-detail-note">${escape(order.cancellationReason)}</p></section>` : ''}
      <section class="orders-detail-section">${actionButtons(order)}</section>`;
    get('orderDetailContent').scrollTop = 0;
    if (!get('orderDetail').open) get('orderDetail').showModal();
    document.body.classList.add('order-detail-open');
  }

  function resetFilters() {
    Object.assign(state, { status: 'ALL', payment: 'ALL', search: '', page: 1 });
    get('orderSearch').value = '';
    get('paymentFilter').value = 'ALL';
    render();
  }

  function exportOrders() {
    const filtered = filteredOrders();
    if (!filtered.length) return;
    const header = ['Mã đơn', 'Ngày đặt', 'Khách hàng', 'Email', 'Số điện thoại', 'Sản phẩm', 'Tổng tiền (VND)', 'Thanh toán', 'Trạng thái đơn'];
    const rows = filtered.map(order => [order.code, `${date(order.date)} ${time(order.date)}`, order.name, order.email, order.phone, order.items.map(item => `${item.name} × ${item.quantity}`).join('; '), order.total, paymentStatuses[order.payment].label, orderStatuses[order.status].label]);
    const cell = value => `"${String(value).replace(/^[=+\-@\t\r]/, char => `'${char}`).replace(/"/g, '""')}"`;
    const csv = '\uFEFF' + [header, ...rows].map(row => row.map(cell).join(',')).join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = 'iuh-don-hang.csv';
    document.body.append(link);
    link.click();
    link.remove();
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    const toast = get('ordersToast');
    toast.textContent = `Đã xuất ${filtered.length} đơn hàng.`;
    toast.hidden = false;
    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(() => { toast.hidden = true; }, 3500);
    if (window.AppleNotification) {
      window.AppleNotification.success(`Đã xuất ${filtered.length} đơn hàng thành công sang tệp CSV.`, 'Xuất dữ liệu');
    }
  }

  let toastTimer;
  function beginAction(event) {
    const button = event.target.closest('[data-action]');
    if (!button || saving) return;
    const order = orders.find(item => item.code === button.dataset.code);
    if (!order) return;
    pendingAction = { order, expectedStatus: order.status, status: button.dataset.action };
    const cancelling = pendingAction.status === 'CANCELLED';
    get('confirmTitle').textContent = `${actionLabels[pendingAction.status]} #${order.code}`;
    get('confirmDescription').textContent = `${orderStatuses[order.status].label} → ${orderStatuses[pendingAction.status].label}. Bạn xác nhận thực hiện?`;
    get('cancelReasonField').hidden = !cancelling;
    get('cancelReason').required = cancelling;
    get('cancelReason').value = '';
    get('orderActionError').hidden = true;
    get('orderConfirm').showModal();
    (cancelling ? get('cancelReason') : get('submitOrderAction')).focus();
  }
  page.addEventListener('click', beginAction);
  get('orderDetailContent').addEventListener('click', beginAction);
  get('dismissOrderConfirm').addEventListener('click', () => get('orderConfirm').close());
  get('orderConfirm').addEventListener('click', event => {
    if (saving) return;
    const dialog = get('orderConfirm');
    const bounds = dialog.getBoundingClientRect();
    if (event.target === dialog && (event.clientX < bounds.left || event.clientX > bounds.right || event.clientY < bounds.top || event.clientY > bounds.bottom)) {
      dialog.close();
    }
  });
  get('orderConfirm').addEventListener('cancel', event => { if (saving) event.preventDefault(); });
  get('orderConfirmForm').addEventListener('submit', async event => {
    event.preventDefault();
    if (saving || !pendingAction) return;
    const action = pendingAction;
    const reason = get('cancelReason').value.trim();
    if (action.status === 'CANCELLED' && !reason) {
      get('orderActionError').textContent = 'Vui lòng nhập lý do hủy đơn.';
      get('orderActionError').hidden = false;
      get('cancelReason').focus();
      return;
    }
    saving = true;
    get('orderConfirmForm').setAttribute('aria-busy', 'true');
    get('orderConfirmForm').querySelectorAll('button, textarea').forEach(element => { element.disabled = true; });
    get('submitOrderAction').textContent = 'Đang lưu…';
    get('orderActionError').hidden = true;
    try {
      const response = await fetch(`${page.dataset.ordersUrl}/${encodeURIComponent(action.order.code)}/status`, {
        method: 'POST', credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json', 'X-Order-CSRF': page.dataset.csrf },
        body: JSON.stringify({ expectedStatus: action.expectedStatus, status: action.status, reason })
      });
      if (response.redirected || response.status === 401) throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
      const result = await response.json();
      if (!response.ok) throw new Error(result.message || 'Không thể cập nhật đơn hàng. Vui lòng tải lại trang.');
      Object.assign(action.order, result);
      renderMetrics();
      render();
      if (get('orderDetail').open && detailCode === action.order.code) openOrder(detailCode);
      get('orderConfirm').close();
      pendingAction = null;
      get('ordersToast').textContent = `Đã cập nhật #${action.order.code}: ${orderStatuses[result.status].label}.`;
      get('ordersToast').hidden = false;
      window.clearTimeout(toastTimer);
      toastTimer = window.setTimeout(() => { get('ordersToast').hidden = true; }, 3500);
      if (window.AppleNotification) {
        window.AppleNotification.success(`Đã cập nhật đơn hàng #${action.order.code}: ${orderStatuses[result.status].label}.`, 'Thành công');
      }
    } catch (error) {
      const errMsg = error instanceof SyntaxError ? 'Không thể đọc phản hồi. Vui lòng tải lại trang.' : error.message;
      get('orderActionError').textContent = errMsg;
      get('orderActionError').hidden = false;
      if (window.AppleNotification) {
        window.AppleNotification.error(errMsg, 'Cập nhật thất bại');
      }
    } finally {
      saving = false;
      get('orderConfirmForm').removeAttribute('aria-busy');
      get('orderConfirmForm').querySelectorAll('button, textarea').forEach(element => { element.disabled = false; });
      get('submitOrderAction').textContent = 'Xác nhận';
    }
  });
  page.addEventListener('click', event => {
    const orderButton = event.target.closest('[data-order]');
    if (orderButton) openOrder(orderButton.dataset.order);
    const statusButton = event.target.closest('[data-status]');
    if (statusButton) {
      state.status = statusButton.dataset.status;
      state.page = 1;
      render();
    }
    const pageButton = event.target.closest('[data-page]');
    if (pageButton && !pageButton.disabled) {
      state.page = Number(pageButton.dataset.page);
      render();
      get('pageButtons').querySelector('[aria-current="page"]').focus();
    }
  });
  get('orderSearch').addEventListener('input', event => {
    state.search = event.target.value;
    state.page = 1;
    render();
  });
  [['paymentFilter', 'payment'], ['orderSort', 'sort'], ['pageSize', 'pageSize']].forEach(([id, key]) => {
    get(id).addEventListener('change', event => {
      state[key] = key === 'pageSize' ? Number(event.target.value) : event.target.value;
      state.page = 1;
      render();
    });
  });
  get('clearFilters').addEventListener('click', resetFilters);
  get('resetEmptyFilters').addEventListener('click', resetFilters);
  get('exportOrders').addEventListener('click', exportOrders);
  get('closeOrderDetail').addEventListener('click', () => get('orderDetail').close());
  get('doneOrderDetail').addEventListener('click', () => get('orderDetail').close());
  get('orderDetail').addEventListener('close', () => document.body.classList.remove('order-detail-open'));
  get('orderDetail').addEventListener('click', event => {
    const dialog = get('orderDetail');
    const bounds = dialog.getBoundingClientRect();
    if (event.target === dialog && (event.clientX < bounds.left || event.clientX > bounds.right || event.clientY < bounds.top || event.clientY > bounds.bottom)) dialog.close();
  });
  document.addEventListener('keydown', event => {
    if (event.key === '/' && !get('orderDetail').open && !event.ctrlKey && !event.metaKey && !event.altKey && !event.target.closest('input, textarea, select, [contenteditable="true"]')) {
      event.preventDefault();
      get('orderSearch').focus();
    }
  });

  function renderMetrics() {
  get('metricTotal').textContent = orders.length;
  get('metricPending').textContent = orders.filter(order => order.status === 'PENDING').length;
  get('metricShipped').textContent = orders.filter(order => order.status === 'SHIPPED').length;
  get('metricRevenue').textContent = money(orders.filter(order => order.payment === 'PAID').reduce((sum, order) => sum + order.total, 0));
  page.querySelectorAll('[data-count]').forEach(counter => {
    counter.textContent = orders.filter(order => counter.dataset.count === 'ALL' || order.status === counter.dataset.count).length;
  });
  }
  renderMetrics();
  render();
})();
