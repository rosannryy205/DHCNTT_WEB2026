/**
 * Client interactions for Bag (Túi hàng)
 * Real-time stepper calculation, dynamic remove/add, empty state switching.
 */
document.addEventListener('DOMContentLoaded', function () {
  const bagItemsContainer = document.getElementById('bagItemsContainer');
  const bagActiveView = document.getElementById('bagActiveView');
  const bagEmptyView = document.getElementById('bagEmptyView');
  const subtotalEl = document.getElementById('bagSubtotal');
  const totalEl = document.getElementById('bagTotal');
  const navBagBadge = document.querySelector('.apple-global-nav .bi-bag + .badge');
  const promoAccordionBtn = document.getElementById('promoAccordionBtn');
  const promoInputCollapse = document.getElementById('promoInputCollapse');
  const promoApplyBtn = document.getElementById('promoApplyBtn');
  const promoInput = document.getElementById('promoInput');
  const discountRow = document.getElementById('discountRow');
  const discountValEl = document.getElementById('discountVal');

  let discountAmount = 0;

  function formatVND(amount) {
    return new Intl.NumberFormat('de-DE').format(amount) + 'đ';
  }

  function parseVND(str) {
    return parseInt(str.replace(/[^\d]/g, ''), 10) || 0;
  }

  // Cập nhật lại tổng tiền và số lượng trên badge
  function recalculateTotals() {
    let subtotal = 0;
    let totalItems = 0;

    const rows = document.querySelectorAll('.bag-item-row');
    rows.forEach(row => {
      const unitPrice = parseInt(row.getAttribute('data-unit-price'), 10) || 0;
      const qtyEl = row.querySelector('.bag-stepper-val');
      const qty = parseInt(qtyEl.textContent, 10) || 1;
      const lineTotal = unitPrice * qty;

      subtotal += lineTotal;
      totalItems += qty;

      const lineTotalEl = row.querySelector('.bag-item-line-total');
      if (lineTotalEl) {
        lineTotalEl.textContent = formatVND(lineTotal);
      }

      // Khống chế trạng thái nút stepper
      const btnMinus = row.querySelector('.btn-qty-minus');
      const btnPlus = row.querySelector('.btn-qty-plus');
      const stock = parseInt(row.getAttribute('data-stock'), 10) || 99;

      if (btnMinus) {
        btnMinus.title = qty <= 1 ? "Xóa khỏi túi hàng" : "Giảm số lượng";
      }
      if (btnPlus) {
        btnPlus.disabled = qty >= stock;
      }
    });

    if (subtotalEl) subtotalEl.textContent = formatVND(subtotal);

    const finalTotal = Math.max(0, subtotal - discountAmount);
    if (totalEl) totalEl.textContent = formatVND(finalTotal);

    // Cập nhật badge trên thanh Global Nav
    if (navBagBadge) {
      navBagBadge.textContent = totalItems;
      navBagBadge.style.display = totalItems > 0 ? 'inline-block' : 'none';
    }

    // Kiểm tra chuyển sang trạng thái Empty State
    if (rows.length === 0) {
      if (bagActiveView) bagActiveView.classList.add('d-none');
      if (bagEmptyView) bagEmptyView.classList.remove('d-none');
    } else {
      if (bagActiveView) bagActiveView.classList.remove('d-none');
      if (bagEmptyView) bagEmptyView.classList.add('d-none');
    }
  }

  // Gắn sự kiện cho từng dòng Bag Item
  function attachRowEvents(row) {
    const btnMinus = row.querySelector('.btn-qty-minus');
    const btnPlus = row.querySelector('.btn-qty-plus');
    const btnRemove = row.querySelector('.bag-link-remove');
    const qtyValEl = row.querySelector('.bag-stepper-val');
    const stock = parseInt(row.getAttribute('data-stock'), 10) || 99;

    if (btnMinus) {
      btnMinus.addEventListener('click', function () {
        let currentQty = parseInt(qtyValEl.textContent, 10) || 1;
        if (currentQty > 1) {
          qtyValEl.textContent = currentQty - 1;
          recalculateTotals();
        } else {
          // Khi giảm về 0: xác nhận xóa
          removeRow(row);
        }
      });
    }

    if (btnPlus) {
      btnPlus.addEventListener('click', function () {
        let currentQty = parseInt(qtyValEl.textContent, 10) || 1;
        if (currentQty < stock) {
          qtyValEl.textContent = currentQty + 1;
          recalculateTotals();
        }
      });
    }

    if (btnRemove) {
      btnRemove.addEventListener('click', function (e) {
        e.preventDefault();
        removeRow(row);
      });
    }
  }

  function removeRow(row) {
    row.classList.add('removing');
    setTimeout(() => {
      row.remove();
      recalculateTotals();
      showToast("Đã xóa sản phẩm khỏi túi hàng.");
    }, 300);
  }

  // Khởi tạo sự kiện cho các item ban đầu
  document.querySelectorAll('.bag-item-row').forEach(attachRowEvents);

  // Toggle mã ưu đãi
  if (promoAccordionBtn && promoInputCollapse) {
    promoAccordionBtn.addEventListener('click', function () {
      const isHidden = promoInputCollapse.classList.contains('d-none');
      if (isHidden) {
        promoInputCollapse.classList.remove('d-none');
      } else {
        promoInputCollapse.classList.add('d-none');
      }
    });
  }

  // Áp dụng mã ưu đãi (mock)
  if (promoApplyBtn && promoInput) {
    promoApplyBtn.addEventListener('click', function () {
      const code = promoInput.value.trim().toUpperCase();
      if (!code) return;

      if (code === 'APPLEVIP' || code === 'DHSTORE') {
        discountAmount = 500000;
        if (discountRow) discountRow.classList.remove('d-none');
        if (discountValEl) discountValEl.textContent = '-' + formatVND(discountAmount);
        recalculateTotals();
        showToast("Áp dụng mã ưu đãi thành công (-500.000đ)");
      } else {
        alert("Mã ưu đãi không hợp lệ. Hãy thử dùng mã: APPLEVIP");
      }
    });
  }

  // Sự kiện Quick Add từ khối Phụ kiện tương thích
  const quickAddBtns = document.querySelectorAll('.btn-quick-add');
  quickAddBtns.forEach(btn => {
    btn.addEventListener('click', function () {
      const id = this.getAttribute('data-id');
      const sku = this.getAttribute('data-sku');
      const name = this.getAttribute('data-name');
      const price = parseInt(this.getAttribute('data-price'), 10) || 0;
      const img = this.getAttribute('data-img');
      const detailUrl = this.getAttribute('data-url') || '/';

      // Kiểm tra xem sản phẩm đã có trong túi chưa
      let existingRow = document.querySelector(`.bag-item-row[data-sku="${sku}"]`);
      if (existingRow) {
        const qtyValEl = existingRow.querySelector('.bag-stepper-val');
        let currentQty = parseInt(qtyValEl.textContent, 10) || 1;
        qtyValEl.textContent = currentQty + 1;
        existingRow.classList.add('animate__animated', 'animate__pulse');
        setTimeout(() => existingRow.classList.remove('animate__animated', 'animate__pulse'), 1000);
        recalculateTotals();
        showToast(`Đã tăng số lượng ${name} trong túi hàng!`);
      } else {
        // Tạo dòng mới
        const newRow = document.createElement('div');
        newRow.className = 'bag-item-row';
        newRow.setAttribute('data-id', id);
        newRow.setAttribute('data-sku', sku);
        newRow.setAttribute('data-unit-price', price);
        newRow.setAttribute('data-stock', '50');

        newRow.innerHTML = `
          <div class="bag-item-image-wrapper">
            <img src="${img}" alt="${name}" class="bag-item-image">
          </div>
          <div class="bag-item-details">
            <div class="bag-item-header">
              <a href="${detailUrl}" class="bag-item-title">${name}</a>
              <div class="bag-item-price-col">
                <div class="bag-item-line-total">${formatVND(price)}</div>
                <div class="bag-item-unit-price">Đơn giá: ${formatVND(price)}</div>
              </div>
            </div>
            <div class="bag-item-specs">
              <span class="bag-item-spec-badge">
                <i class="bi bi-shield-check text-primary"></i> Chính hãng Apple
              </span>
              <span class="bag-item-spec-badge">SKU: ${sku}</span>
            </div>
            <div class="bag-item-actions">
              <div class="bag-stepper">
                <button type="button" class="bag-stepper-btn btn-qty-minus" title="Giảm số lượng">
                  <i class="bi bi-dash"></i>
                </button>
                <span class="bag-stepper-val">1</span>
                <button type="button" class="bag-stepper-btn btn-qty-plus" title="Tăng số lượng">
                  <i class="bi bi-plus"></i>
                </button>
              </div>
              <div class="bag-action-links">
                <button type="button" class="bag-link-remove">
                  <i class="bi bi-trash3"></i> Xóa
                </button>
              </div>
            </div>
          </div>
        `;

        if (bagItemsContainer) {
          bagItemsContainer.appendChild(newRow);
          attachRowEvents(newRow);
          recalculateTotals();
          showToast(`Đã thêm ${name} vào túi hàng!`);

          // Cuộn mượt lên vị trí sản phẩm mới
          newRow.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }
    });
  });

  // Toast Notification helper
  function showToast(message) {
    let toast = document.getElementById('bagActionToast');
    if (!toast) {
      toast = document.createElement('div');
      toast.id = 'bagActionToast';
      toast.style.cssText = `
        position: fixed;
        bottom: 24px;
        right: 24px;
        background-color: #1d1d1f;
        color: #ffffff;
        padding: 12px 24px;
        border-radius: 9999px;
        font-size: 14px;
        z-index: 9999;
        box-shadow: 0 4px 20px rgba(0,0,0,0.25);
        display: flex;
        align-items: center;
        gap: 8px;
        transition: opacity 0.3s ease, transform 0.3s ease;
      `;
      document.body.appendChild(toast);
    }
    toast.innerHTML = `<i class="bi bi-check-circle-fill text-primary"></i> ${message}`;
    toast.style.opacity = '1';
    toast.style.transform = 'translateY(0)';

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
    }, 3000);
  }

  // Khởi chạy tính toán ban đầu
  recalculateTotals();
});
